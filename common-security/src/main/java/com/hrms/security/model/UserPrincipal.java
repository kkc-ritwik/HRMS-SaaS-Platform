package com.hrms.security.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private String id;
    private String tenantId;
    private String employeeId;
    private String email;
    private String password;
    private String fullName;
    @Builder.Default private Set<String> roles = new HashSet<>();
    @Builder.Default private Set<String> permissions = new HashSet<>();
    @Builder.Default private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> auths = new HashSet<>();
        roles.forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
        permissions.forEach(p -> auths.add(new SimpleGrantedAuthority(p)));
        return auths;
    }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
