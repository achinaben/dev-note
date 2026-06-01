package com.scm.oms.integration;

public class StubWmsGateway implements WmsGateway {
    @Override
    public String createOutbound(String packageNo, String orderNo) {
        return "OB-MOCK";
    }

    @Override
    public void ship(String outboundNo) {
        ship(outboundNo, null);
    }

    @Override
    public void ship(String outboundNo, String waybillNo) {
        // no-op for unit tests
    }
}
