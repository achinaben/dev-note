package com.scm.wms.pick;

import java.util.Optional;

public interface PickConfirmStore {

    boolean exists(String operationId);

    void save(String operationId, String outboundNo);

    Optional<String> findOutboundNo(String operationId);
}
