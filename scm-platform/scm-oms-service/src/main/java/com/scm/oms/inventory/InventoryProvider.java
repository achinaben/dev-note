package com.scm.oms.inventory;

public enum InventoryProvider {
    /** 进程内内存，不调远程 */
    LOCAL,
    /** mock-inventory :8087 */
    MOCK,
    /** WMS 分配服务 :8082/inventory/v1 */
    WMS
}
