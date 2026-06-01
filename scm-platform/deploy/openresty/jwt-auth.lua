-- 网关 JWT 校验：lua-resty-openidc 直连 JWKS 校验签名，再校验路由 scope。
local _M = {}

local function jwt_issuer()
    return os.getenv("SCM_JWT_ISSUER") or "http://localhost:8180/realms/scm"
end

local function jwt_options()
    local issuer = jwt_issuer()
    local jwks_uri = os.getenv("SCM_JWT_JWKS_URI")
    local discovery_uri = os.getenv("SCM_JWT_DISCOVERY_URI")

    local discovery = discovery_uri or (issuer .. "/.well-known/openid-configuration")
    if jwks_uri and jwks_uri ~= "" then
        discovery = {
            issuer = issuer,
            jwks_uri = jwks_uri
        }
    end

    return {
        discovery = discovery,
        ssl_verify = os.getenv("SCM_JWT_SSL_VERIFY") or "no",
        token_signing_alg_values_expected = { os.getenv("SCM_JWT_ALG") or "RS256" },
        accept_none_alg = false,
        accept_unsupported_alg = false,
        jwk_expires_in = tonumber(os.getenv("SCM_JWT_JWKS_EXPIRES_IN")) or 86400,
        discovery_expires_in = tonumber(os.getenv("SCM_JWT_DISCOVERY_EXPIRES_IN")) or 86400,
        cache_segment = os.getenv("SCM_JWT_CACHE_SEGMENT") or "scm-gateway-jwt",
        timeout = tonumber(os.getenv("SCM_JWT_HTTP_TIMEOUT_MS")) or 3000
    }
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

local function unauthorized(message)
    ngx.status = 401
    ngx.header["Content-Type"] = "application/json"
    local cjson = require "cjson.safe"
    ngx.say(cjson.encode({ code = "GATEWAY_401", message = message }))
    return ngx.exit(401)
end

function _M.validate(required_scope, require_bearer)
    local auth = ngx.var.http_authorization
    if require_bearer and (not auth or auth == "") then
        return unauthorized("missing_bearer")
    end
    if not auth or auth == "" then
        return true
    end
    if not auth:match("^[Bb]earer%s+.+$") then
        return unauthorized("invalid Authorization")
    end

    local openidc = require "resty.openidc"
    local payload, err = openidc.bearer_jwt_verify(jwt_options())
    if not payload then
        return unauthorized(err or "jwt_error")
    end
    if payload.iss and payload.iss ~= jwt_issuer() then
        return unauthorized("bad_issuer")
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
