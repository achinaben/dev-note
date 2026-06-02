#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
export KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
export SCM_E2E_OMS_AUTH="${SCM_E2E_OMS_AUTH:-none}"
mvn -pl scm-integration-tests -Pe2e-kafka test -Dcucumber.filter.tags='@E2E-K05'
