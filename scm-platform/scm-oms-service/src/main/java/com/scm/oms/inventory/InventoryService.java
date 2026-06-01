package com.scm.oms.inventory;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private final InventorySettings settings;
    private final InventoryRemoteClient remoteClient;
    private final Map<String, String> localStatus = new ConcurrentHashMap<>();

    public InventoryService(InventorySettings settings, InventoryRemoteClient remoteClient) {
        this.settings = settings;
        this.remoteClient = remoteClient;
    }

    public void reserve(String orderNo) {
        if (!settings.useRemote()) {
            localStatus.put(orderNo, "RESERVED");
            return;
        }
        remoteClient.reserve(orderNo);
        localStatus.put(orderNo, "RESERVED");
    }

    public void confirm(String orderNo) {
        if (!settings.useRemote()) {
            localStatus.put(orderNo, "CONFIRMED");
            return;
        }
        remoteClient.confirm(orderNo);
        localStatus.put(orderNo, "CONFIRMED");
    }

    public void release(String orderNo) {
        if (!settings.useRemote()) {
            localStatus.put(orderNo, "RELEASED");
            return;
        }
        remoteClient.release(orderNo);
        localStatus.put(orderNo, "RELEASED");
    }

    public String status(String orderNo) {
        return localStatus.getOrDefault(orderNo, "NONE");
    }

    public InventoryProvider provider() {
        return settings.getProvider();
    }
}
