package org.example.file_api.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.file_api.security.model.LoginUser;
import org.example.file_api.security.context.TenantContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 对 /tenant/** 请求读取 tenant-id，并确认当前用户属于该租户。
 */
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "tenant-id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/tenant/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 没有认证身份时，交给后面的 Spring Security 规则返回 401。
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenantHeader = request.getHeader(TENANT_HEADER);
        if (tenantHeader == null || tenantHeader.isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "缺少 tenant-id 请求头");
            return;
        }

        final Long tenantId;
        try {
            tenantId = Long.valueOf(tenantHeader);
        } catch (NumberFormatException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "tenant-id 必须是数字");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof LoginUser loginUser)
                || !loginUser.belongsToTenant(tenantId)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "当前用户不属于该租户");
            return;
        }

        try {
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            // Servlet 线程会被复用，必须清理，避免下一个请求读到旧租户。
            TenantContext.clear();
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
