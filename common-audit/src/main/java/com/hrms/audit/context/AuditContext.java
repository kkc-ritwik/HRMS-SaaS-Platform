package com.hrms.audit.context;

/**
 * ThreadLocal carrier for who/what/where details about the current request,
 * read by {@link com.hrms.audit.listener.AuditEntityListener} and HTTP filters.
 * Services should populate this in a request filter (see RequestContextFilter in common-security).
 */
public final class AuditContext {

    public record Snapshot(String actorId, String actorEmail, String ipAddress, String userAgent, String requestId) {}

    private static final ThreadLocal<Snapshot> CTX = new ThreadLocal<>();

    private AuditContext() {}

    public static void set(Snapshot s) { CTX.set(s); }
    public static Snapshot get() { return CTX.get(); }
    public static void clear() { CTX.remove(); }
}
