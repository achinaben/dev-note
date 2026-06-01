package com.scm.common.tenant;

/**
 * 请求级 org_id 透传，默认夹具 ORG001。
 */
public final class OrgIdContext {
    public static final String DEFAULT_ORG = "ORG001";
    public static final String HEADER = "X-Org-Id";

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private OrgIdContext() {
    }

    public static void set(String orgId) {
        if (orgId == null || orgId.isBlank()) {
            HOLDER.remove();
        } else {
            HOLDER.set(orgId.trim());
        }
    }

    public static String get() {
        String v = HOLDER.get();
        return v == null || v.isBlank() ? DEFAULT_ORG : v;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
