package org.example.file_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 给 Controller、Service 等业务代码提供当前登录用户信息。
 */
@Component
public class CurrentUserProvider {

    public String getUsername() {
        return requireAuthentication().getName();
    }

    public Long getUserId() {
        Object principal = requireAuthentication().getPrincipal();

        if (!(principal instanceof LoginUser loginUser)) {
            throw new IllegalStateException("当前认证身份中没有业务用户 ID");
        }

        return loginUser.getUserId();
    }

    private Authentication requireAuthentication() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("当前请求没有已认证用户");
        }

        return authentication;
    }
}
