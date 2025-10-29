// src/main/java/com/cat/connect/tenant/TenantContext.java
package com.cat.connect.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT_COMPANY = new ThreadLocal<>();
    private TenantContext() {}

    public static void setCompanyId(Long companyId) { CURRENT_COMPANY.set(companyId); }
    public static Long getCompanyId() { return CURRENT_COMPANY.get(); }
    public static void clear() { CURRENT_COMPANY.remove(); }
}
