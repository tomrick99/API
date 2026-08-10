package org.example.file_api.security.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.file_api.security.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 每个 HTTP 请求执行一次，从 Authorization 请求头中读取和验证 JWT。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            TokenService tokenService,
            UserDetailsService userDetailsService
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 前端要发送Authorization: Bearer token...
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 如果没有 Bearer Token 时，本过滤器不负责决定是否放行，继续交给后面的安全规则。
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 取出JWT 把前面的Bearer去掉了
        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            // 验证并且获得用户名
            String username = tokenService.extractUsername(token);

            // 避免覆盖请求中已经存在的认证身份。
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // 用查到的用户去得到密码哈希 ROLE_USER
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                // 创建已认证的对象
                var authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 把所有东西放到一个盒子里
                SecurityContext securityContext =
                        SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
            }

            //继续执行过滤链
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\":\"Token 无效或已过期\"}");
        }
    }
}
