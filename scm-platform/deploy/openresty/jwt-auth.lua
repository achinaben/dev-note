-- 网关 JWT 校验：lua-resty-openidc 直连 JWKS 验 RS256，再校验 exp/iss/scope。
local _M = {}

local function getenv(name, default)
    local value = os.getenv(name)
    if value == nil or value == "" then
        return default
    end
    return value
end

local function issuer()
    return getenv("SCM_JWT_ISSUER", "http://keycloak:8080/realms/scm")
end

local function jwks_uri()
    local configured = os.getenv("SCM_JWT_JWKS_URI")
    if configured and configured ~= "" then
        return configured
    end
    return issuer():gsub("/+$", "") .. "/protocol/openid-connect/certs"
end

local function openidc_opts()
    return {
        discovery = {
            jwks_uri = jwks_uri()
        },
        token_signing_alg_values_expected = { "RS256" },
        timeout = tonumber(getenv("SCM_JWT_TIMEOUT_MS", "2000")),
        jwk_expires_in = tonumber(getenv("SCM_JWT_JWKS_CACHE_SECONDS", "3600")),
        ssl_verify = getenv("SCM_JWT_SSL_VERIFY", "no")
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

local function json_error(status, code, message)
    ngx.status = status
    ngx.header["Content-Type"] = "application/json"
    ngx.say('{"code":"' .. code .. '","message":"' .. message .. '"}')
    return ngx.exit(status)
end

function _M.validate(required_scope, require_bearer)
    local auth = ngx.var.http_authorization
    if require_bearer and (not auth or auth == "") then
        return json_error(401, "GATEWAY_401", "missing_bearer")
    end
    if not auth or auth == "" then
        return true
    end
    if not auth:match("^[Bb]earer%s+.+$") then
        return json_error(401, "GATEWAY_401", "invalid Authorization")
    end
    local openidc = require "resty.openidc"
    local payload, err = openidc.bearer_jwt_verify(openidc_opts())
    if not payload then
        ngx.log(ngx.WARN, "JWT RS256 verification failed: ", err or "jwt_error")
        return json_error(401, "GATEWAY_401", "jwt_invalid")
    end
    local exp = tonumber(payload.exp)
    if exp and exp < ngx.time() then
        return json_error(401, "GATEWAY_401", "token_expired")
    end
    if payload.iss and payload.iss ~= issuer() then
        return json_error(401, "GATEWAY_401", "bad_issuer")
    end
    if not scope_ok(payload, required_scope) then
        return json_error(403, "GATEWAY_403", "insufficient_scope")
    end
    return true
end

return _M
