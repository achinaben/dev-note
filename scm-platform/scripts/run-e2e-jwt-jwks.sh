#!/usr/bin/env bash
# Keycloak RS256 + OMS jwt,jwt-jwks
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export SCM_JWT_STRICT=1
cd "$ROOT"
mvn -pl scm-integration-tests -Pe2e-jwt-jwks test
