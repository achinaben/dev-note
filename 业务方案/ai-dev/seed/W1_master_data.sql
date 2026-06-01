-- W1 种子数据 — 与 fixtures.yaml 对齐
INSERT INTO org (org_id, org_code, org_name, ledger_id, base_currency, status, version)
VALUES (1, 'ORG001', '测试组织', 1, 'CNY', 1, 0);

INSERT INTO material (material_id, material_code, material_name, uom_code, batch_managed, status, mdm_version)
VALUES (1, 'M001', '测试商品物料', 'EA', 0, 1, 1);

INSERT INTO material_mapping (material_id, external_system, external_code)
VALUES (1, 'OMS', 'SKU001'), (1, 'WMS', 'SKU001');

INSERT INTO partner (partner_id, partner_code, partner_type, credit_limit, credit_used, status)
VALUES (1, 'C10001', 'CUSTOMER', 500000.0000, 0, 1);

INSERT INTO warehouse (wh_id, wh_code, wh_name, erp_wh_code, status)
VALUES (1, 'WH-SH-01', '上海仓', 'WH-SH-01', 1);

-- posting_rule 示例：出库成本
INSERT INTO posting_rule (rule_id, event_type, org_id, debit_account, credit_account, amount_expr, priority, enabled)
VALUES (1, 'WMS_OUTBOUND_SHIPPED', NULL, '6401', '1405', 'sum(line.qty*line.unit_cost)', 100, 1);
