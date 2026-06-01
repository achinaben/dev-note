-- 网关 JWT 轻量校验：Bearer 格式、exp、路由 scope（与 OMS jwt profile 对齐）
local _M = {}

local function b64url_decode(input)
    local rem = #input % 4
    if rem > 0 then
        input = input .. string.rep("=", 4 - rem)
    end
    input = input:gsub("-", "+"):gsub("_", "/")
    return ngx.decode_base64(input)
end

local function parse_payload(token)
    local parts = {}
    for part in string.gmatch(token, "[^.]+") do
        parts[#parts + 1] = part
    end
    if #parts < 2 then
        return nil, "malformed_jwt"
    end
    local raw = b64url_decode(parts[2])
    if not raw then
        return nil, "bad_payload"
    end
    local cjson = require "cjson.safe"
    local payload, err = cjson.decode(raw)
    if not payload then
        return nil, err or "json_error"
    end
    return payload
end

local function role_list(payload)
    local roles = {}
    local ra = payload.realm_access
    if ra and ra.roles then
        for _, r in ipairs(ra.roles) do
            roles[#roles + 1] = r
        end
    end
    local res = payload.resource_access
    if res then
        for _, client in pairs(res) do
            if client.roles then
                for _, r in ipairs(client.roles) do
                    roles[#roles + 1] = r
                end
            end
        end
    end
    return roles
end

local function scope_ok(payload, required)
    if not required or required == "" then
        return true
    end
    local scope = payload.scope or payload.scp or ""
    if type(scope) == "table" then
        scope = table.concat(scope, " ")
    end
    if string.find(" " .. scope .. " ", " " .. required .. " ", 1, true) ~= nil then
        return true
    end
    for _, role in ipairs(role_list(payload)) do
        if role == required then
            return true
        end
    end
    return false
end

function _M.validate(required_scope, require_bearer)
    local auth = ngx.var.http_authorization
    if require_bearer and (not auth or auth == "") then
        ngx.status = 401
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_401","message":"missing_bearer"}')
        return ngx.exit(401)
    end
    if not auth or auth == "" then
        return true
    end
    local token = auth:match("^[Bb]earer%s+(.+)$")
    if not token then
        ngx.status = 401
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_401","message":"invalid Authorization"}')
        return ngx.exit(401)
    end
    local payload, err = parse_payload(token)
    if not payload then
        ngx.status = 401
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_401","message":"' .. (err or "jwt_error") .. '"}')
        return ngx.exit(401)
    end
    local exp = tonumber(payload.exp)
    if exp and exp < ngx.time() then
        ngx.status = 401
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_401","message":"token_expired"}')
        return ngx.exit(401)
    end
    local issuer = os.getenv("SCM_JWT_ISSUER") or "http://localhost:8180/realms/scm"
    if payload.iss and payload.iss ~= issuer then
        ngx.status = 401
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_401","message":"bad_issuer"}')
        return ngx.exit(401)
    end
    if not scope_ok(payload, required_scope) then
        ngx.status = 403
        ngx.header["Content-Type"] = "application/json"
        ngx.say('{"code":"GATEWAY_403","message":"insufficient_scope"}')
        return ngx.exit(403)
    end
    return true
end

return _M
