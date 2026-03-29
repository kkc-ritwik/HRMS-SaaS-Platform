package com.hrms.common.constants;

public final class AppConstants {
    private AppConstants() {}
    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String SYSTEM_USER = "SYSTEM";
    public static final String TOPIC_EMPLOYEE = "hr.employee.events";
    public static final String TOPIC_LEAVE = "hr.leave.events";
    public static final String TOPIC_PAYROLL = "hr.payroll.events";
    public static final String TOPIC_NOTIFICATION = "hr.notification.events";
    public static final String TOPIC_AUDIT = "hr.audit.events";
}
