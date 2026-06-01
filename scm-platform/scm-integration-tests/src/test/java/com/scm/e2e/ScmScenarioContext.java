package com.scm.e2e;

public final class ScmScenarioContext {
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private ScmScenarioContext() {
    }

    public static State get() {
        return STATE.get();
    }

    public static void clear() {
        STATE.remove();
    }

    public static class State {
        public String clientToken = "ct-fix-" + System.currentTimeMillis();
        public String orderNo;
        public String packageNo;
        public String outboundNo;
        public String bizKey;
        public int httpStatus;
        public String lastOrderNo2;
        public String notifyId;
        public String errorCode;
        public String afterSaleNo;
        /** WMS 交接发运时写入的运单号，供 TMS 联动断言 */
        public String handoverWaybill;
    }
}
