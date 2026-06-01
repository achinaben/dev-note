package com.scm.oms.inventory;

public final class InventoryTestSupport {

    private InventoryTestSupport() {
    }

    public static InventoryService localInventory() {
        InventorySettings settings = new InventorySettings();
        settings.setProvider(InventoryProvider.LOCAL);
        settings.setRemoteEnabled(false);
        return new InventoryService(settings, new InventoryRemoteClient(settings));
    }
}
