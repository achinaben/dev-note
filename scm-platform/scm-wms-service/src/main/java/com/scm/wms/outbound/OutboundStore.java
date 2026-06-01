package com.scm.wms.outbound;

import java.util.Optional;

public interface OutboundStore {
    Optional<OutboundRecord> findByPackageNo(String packageNo);

    Optional<OutboundRecord> findByOutboundNo(String outboundNo);

    Optional<String> findOutboundNoBySourceOrder(String sourceOrderNo);

    OutboundRecord insert(OutboundRecord record);

    void updateStatus(String outboundNo, String status);

    long countByPackageNo(String packageNo);
}
