#!/usr/bin/env bash
# 一键启动四服务（Linux / CI）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/scripts/logs"
mkdir -p "$LOG_DIR"
WAIT_SECONDS="${WAIT_SECONDS:-120}"
USE_KAFKA="${USE_KAFKA:-0}"
USE_JDBC="${USE_JDBC:-0}"
USE_WMS_STRICT="${USE_WMS_STRICT:-0}"
USE_OMS_JWT="${USE_OMS_JWT:-0}"
USE_OMS_JWT_JWKS="${USE_OMS_JWT_JWKS:-0}"
JAVA_HOME="${JAVA_HOME:-}"
export MAVEN_OPTS="${MAVEN_OPTS:--Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m}"

if [[ -z "$JAVA_HOME" ]] && command -v java >/dev/null 2>&1; then
  JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
fi
export JAVA_HOME

PROFILE_ARG=""
PROFILES=()
[[ "$USE_JDBC" == "1" ]] && PROFILES+=("jdbc")
[[ "$USE_KAFKA" == "1" ]] && PROFILES+=("kafka")
if [[ ${#PROFILES[@]} -gt 0 ]]; then
  PROFILE_ARG="-Dspring-boot.run.profiles=$(IFS=,; echo "${PROFILES[*]}")"
fi
if [[ "$USE_JDBC" == "1" ]]; then
  echo "启动 MySQL..."
  (cd "$ROOT" && docker compose up -d mysql-erp mysql-oms mysql-wms mysql-tms)
  for p in 3307 3308 3309 3310; do
    for _ in $(seq 1 45); do
      if (echo >/dev/tcp/127.0.0.1/$p) 2>/dev/null; then break; fi
      sleep 2
    done
  done
fi
if [[ "$USE_KAFKA" == "1" ]]; then
  echo "启动 Kafka..."
  (cd "$ROOT" && docker compose up -d kafka)
  for _ in $(seq 1 30); do
    if (echo >/dev/tcp/127.0.0.1/9092) 2>/dev/null; then break; fi
    sleep 2
  done
fi

echo "mvn install -DskipTests ..."
(cd "$ROOT" && mvn -q install -DskipTests)

start_one() {
  local module="$1" port="$2"
  local extra_profiles="${3:-}"
  local log="$LOG_DIR/${module}.log"
  local run_profiles="$PROFILE_ARG"
  if [[ -n "$extra_profiles" ]]; then
    if [[ -n "$run_profiles" ]]; then
      run_profiles="${run_profiles},${extra_profiles}"
    else
      run_profiles="-Dspring-boot.run.profiles=${extra_profiles}"
    fi
  fi
  (cd "$ROOT" && mvn -q -pl "$module" spring-boot:run \
    -Dspring-boot.run.jvmArguments="$MAVEN_OPTS" $run_profiles \
    >"$log" 2>&1) &
  echo "已启动 $module -> :$port  日志: $log"
  sleep 8
}

WMS_EXTRA=""
[[ "$USE_WMS_STRICT" == "1" ]] && WMS_EXTRA="wms-strict"
OMS_EXTRA=""
if [[ "$USE_OMS_JWT" == "1" ]]; then
  OMS_EXTRA="jwt"
  [[ "$USE_OMS_JWT_JWKS" == "1" ]] && OMS_EXTRA="jwt,jwt-jwks"
fi

start_one scm-oms-service 8081 "$OMS_EXTRA"
start_one scm-wms-service 8082 "$WMS_EXTRA"
start_one scm-tms-service 8083
start_one scm-erp-service 8084
start_one scm-mock-pay 8085
start_one scm-mock-carrier 8086
start_one scm-mock-inventory 8087

deadline=$((SECONDS + WAIT_SECONDS))
while (( SECONDS < deadline )); do
  ok=1
  for p in 8081 8082 8083 8084 8085 8086 8087; do
    if ! (echo >/dev/tcp/127.0.0.1/$p) 2>/dev/null; then ok=0; break; fi
  done
  if [[ $ok -eq 1 ]]; then
    echo "四服务已就绪。"
    [[ "$USE_WMS_STRICT" == "1" ]] && echo "严格出库 E2E: mvn -pl scm-integration-tests -Pe2e-strict-outbound test"
    [[ "$USE_JDBC" == "1" && "$USE_WMS_STRICT" == "1" ]] && echo "严格+jdbc E2E: mvn -pl scm-integration-tests -Pe2e-strict-jdbc test"
    exit 0
  fi
  sleep 2
done
echo "超时：请查看 $LOG_DIR" >&2
exit 1
