package com.hrms.events.model;

/** Canonical Kafka topic names — all services must publish/subscribe through these constants. */
public final class Topics {
    private Topics() {}

    // Core
    public static final String EMPLOYEE       = "hrms.employee";        // created, updated, status_changed, deleted
    public static final String ORG_STRUCTURE  = "hrms.org-structure";   // dept/designation/location

    // Auth
    public static final String AUTH           = "hrms.auth";            // login, logout, password_reset, mfa

    // Leave / Attendance
    public static final String LEAVE          = "hrms.leave";           // applied, approved, rejected, cancelled
    public static final String ATTENDANCE     = "hrms.attendance";      // punch, regularization

    // Payroll
    public static final String PAYROLL        = "hrms.payroll";         // run.created/processed/locked/paid; payslip.published

    // Recruitment
    public static final String RECRUITMENT    = "hrms.recruitment";     // requisition, application, interview, offer

    // Onboarding / Offboarding
    public static final String ONBOARDING     = "hrms.onboarding";
    public static final String OFFBOARDING    = "hrms.offboarding";

    // Performance / LMS / Engagement
    public static final String PERFORMANCE    = "hrms.performance";
    public static final String LMS            = "hrms.lms";
    public static final String ENGAGEMENT     = "hrms.engagement";

    // Operational
    public static final String EXPENSE        = "hrms.expense";
    public static final String ASSET          = "hrms.asset";
    public static final String DOCUMENT       = "hrms.document";
    public static final String WORKFLOW       = "hrms.workflow";
    public static final String NOTIFICATION   = "hrms.notification";    // outbound delivery requests
    public static final String COMPLIANCE     = "hrms.compliance";
    public static final String HELPDESK       = "hrms.helpdesk";
    public static final String SOCIAL         = "hrms.social";
    public static final String TRAVEL         = "hrms.travel";
    public static final String TIMESHEET      = "hrms.timesheet";
    public static final String CASES          = "hrms.cases";

    // System
    public static final String SEARCH_INDEX   = "hrms.search.index";    // CDC → Elasticsearch
    public static final String WEBHOOKS       = "hrms.webhooks";        // outbound to integrations
    public static final String DLQ_SUFFIX     = ".dlq";
}
