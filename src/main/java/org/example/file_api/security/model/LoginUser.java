package org.example.file_api.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Spring Security 使用的当前登录用户，同时携带业务 userId。
 */
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<Long> tenantIds;

    public LoginUser(
            Long userId,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Set<Long> tenantIds
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.tenantIds = Set.copyOf(tenantIds);
    }

    public Long getUserId() {
        return userId;
    }

    public boolean belongsToTenant(Long tenantId) {
        return tenantIds.contains(tenantId);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
