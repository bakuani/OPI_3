package ru.ani.web.models;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enumeration of user roles. Implements GrantedAuthority for Spring Security.
 */
public enum UserRole implements GrantedAuthority {
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
