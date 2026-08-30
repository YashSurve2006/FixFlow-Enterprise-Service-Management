package com.fixflow.security;

import com.fixflow.model.Role;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class UserSecurityContext implements SecurityContext {
    private final Integer userId;
    private final Role role;

    public UserSecurityContext(Integer userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> String.valueOf(userId);
    }

    @Override
    public boolean isUserInRole(String roleName) {
        return this.role.name().equals(roleName);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
