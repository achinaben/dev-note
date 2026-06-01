#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mvn -pl scm-integration-tests -Pe2e test -Dcucumber.filter.tags='@smoke and @e2e'
