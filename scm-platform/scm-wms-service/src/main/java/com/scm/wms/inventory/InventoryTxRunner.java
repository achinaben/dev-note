package com.scm.wms.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class InventoryTxRunner {

    private final TransactionTemplate transactionTemplate;

    public InventoryTxRunner(@Autowired(required = false) PlatformTransactionManager transactionManager) {
        this.transactionTemplate = transactionManager != null ? new TransactionTemplate(transactionManager) : null;
    }

    public <T> T run(Supplier<T> work) {
        if (transactionTemplate == null) {
            return work.get();
        }
        return transactionTemplate.execute(status -> work.get());
    }

    public void runVoid(Runnable work) {
        run(() -> {
            work.run();
            return null;
        });
    }
}
