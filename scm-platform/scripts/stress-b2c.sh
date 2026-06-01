#!/usr/bin/env bash
# B2C 轻量压测：建单 + mock-pay
set -euo pipefail
ITERATIONS="${1:-20}"
OMS="${OMS_BASE:-http://localhost:8081}"
PAY="${PAY_BASE:-http://localhost:8085}"
ok=0
fail=0
echo "压测 ${ITERATIONS} 次 -> ${OMS}"
for i in $(seq 1 "$ITERATIONS"); do
  token="ct-stress-${i}-$RANDOM"
  order_no=$(curl -sf -X POST "${OMS}/api/v1/orders" \
    -H "Idempotency-Key: ${token}" -H "Content-Type: application/json" \
    -d "{\"client_token\":\"${token}\",\"buyer_id\":\"U10001\",\"channel\":\"APP\",\"address_id\":\"ADDR100\",\"lines\":[{\"sku_id\":\"SKU001\",\"qty\":\"2\",\"warehouse_id\":\"WH-SH-01\"}]}" \
    | python -c "import sys,json; print(json.load(sys.stdin)['data']['orders'][0]['order_no'])" 2>/dev/null) || { fail=$((fail+1)); continue; }
  curl -sf -X POST "${PAY}/trigger" -H "Content-Type: application/json" \
    -d "{\"order_no\":\"${order_no}\",\"notify_id\":\"notify-${token}\"}" >/dev/null || { fail=$((fail+1)); continue; }
  ok=$((ok+1))
done
echo "完成: 成功=${ok} 失败=${fail}"
