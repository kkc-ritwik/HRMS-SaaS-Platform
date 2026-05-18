package com.hrms.onboarding.provisioning;

/** Vendor-agnostic interface for creating / suspending IT identities on day-0 / last-working-day. */
public interface IdentityProvisioner {

    /** True if this provisioner handles the given platform code. */
    boolean supports(String platform);   // GOOGLE_WORKSPACE / AZURE_AD / OKTA / LDAP

    /** Provision a new user, return the provider-side ID. */
    String provision(NewIdentity identity);

    /** Suspend or delete the user (called on offboarding). */
    void deactivate(String externalId);

    /** Add the user to a group / OU. */
    void addToGroup(String externalId, String groupId);

    /** Reset / temporary password. */
    String resetPassword(String externalId);

    /** Carrier record for the create-user request. */
    record NewIdentity(String firstName, String lastName, String workEmail,
                       String temporaryPassword, String department, String designation,
                       String managerEmail, String orgUnit) {}
}
