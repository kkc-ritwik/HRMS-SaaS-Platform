package com.hrms.security.scim;

import java.util.List;
import java.util.Map;

/**
 * Contract implemented by service-auth — translates SCIM 2.0 JSON envelopes
 * into operations on the local User / Role tables. Returns standard SCIM resource maps:
 *   {
 *     "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
 *     "id": "...", "userName": "...", "name": {...},
 *     "emails": [...], "active": true, "meta": {...}
 *   }
 */
public interface ScimUserAdapter {

    /** SCIM filter: userName eq "x" / emails.value eq "y" / etc. (basic support). */
    List<Map<String, Object>> listUsers(String filter, int startIndex, int count);

    Map<String, Object> getUser(String id);

    Map<String, Object> createUser(Map<String, Object> body);

    Map<String, Object> replaceUser(String id, Map<String, Object> body);

    Map<String, Object> patchUser(String id, Map<String, Object> body);

    /** Soft-delete / suspend the user (SCIM DELETE → set active=false). */
    void deactivateUser(String id);
}
