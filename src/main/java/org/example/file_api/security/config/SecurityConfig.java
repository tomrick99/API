package org.example.file_api.security.config;

import org.example.file_api.security.filter.JwtAuthenticationFilter;
import org.example.file_api.security.model.LoginUser;
import org.example.file_api.security.filter.TenantContextFilter;
import org.example.file_api.security.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Set;

/**
 * 这个类定义 HTTP 请求进入 Controller 前必须经过的安全规则。
 *
 * @Configuration告诉Spring这是一个配置类
 * @EnableWebSecurity 是开启web请求的安全功能 让SpringSecurity接管请求 用类内规定的规则
 * 简单来说就是开启保安系统
 *
 * 整个链路:
 * 项目启动:
 *  Spring调用这个方法使用这里面的HttpSecurity配置规则
 *  ->build创建整一个链路->再交给spring管理这条过滤链
 *
 *  用户发送请求
 *  -> 请求进入过滤链
 *  -> 如果是/auth/login, permittAll放行
 *  -> permitAll 登录Controller Controller再检查账号密码
 *  -> 如果是其他的链路 检查是否Authenticated
 *  -> 已认证才进入Controller
 *  -> 未认证返回401
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //  1 根据用户名查询用户
    // 请Spring执行这个方法 并且把方法返回SecurityFilterChain的这个对象放进Spring容器里面管理
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenService tokenService,
            UserDetailsService userDetailsService
    ) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(tokenService, userDetailsService);
        TenantContextFilter tenantContextFilter = new TenantContextFilter();

        return http
                // 暂时关闭CSRF防护
                .csrf(csrf -> csrf.disable())
                // STATELESS无状态 也就是服务端不通过HTTP Session记住用户是否登录  每一次的请求都必须重新拿token登陆
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 关闭formLogin 写的是前后端分离项目 不用Spring自动生成一个登录网页 用的是REST API
                .formLogin(form -> form.disable())
                // 用的是Bearer Token验证 不用base64用户密码登录
                .httpBasic(basic -> basic.disable())
                // 没有认证身份时返回 401，而不是使用默认的 403。
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\":\"请先登录\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\":\"权限不足\"}");
                        })
                )
                // 是配置授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 匹配路径为/auth/login的请求  permit是任何人都可以访问的意思 不要求要Token的登录
                        .requestMatchers("/auth/login").permitAll()
                        // 已登录还不够，访问 /admin/** 必须拥有 ROLE_ADMIN。
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 除了上面的/auth/login以外的所有请求 authenticated检查的是SpringSecurity中当前请求是否已有认证身份
                        .anyRequest().authenticated()
                )
                // 先验证 JWT，再执行后续的账号密码认证过滤器和授权判断。
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                // tenant-id 依赖当前用户信息，所以必须在 JWT 认证之后执行。
                .addFilterAfter(tenantContextFilter, JwtAuthenticationFilter.class)
                // 前面规则写完 然后把他们组装成一个真正的一个链路
                .build();
    }

    /**
     * 2 密码处理工具
     * 提供密码哈希和密码校验工具。
     * 数据库只应该保存编码后的密码，不能保存用户提交的原始密码。
     *
     * Spring启动
     *  -> 调用passwordEncoder()
     *  -> 创建BCrypt对象
     *  -> 放进Spring容器
     *  -> 后面的登录逻辑可以注入并且使用它
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 学习阶段先提供一个内存用户，后面再替换为从数据库查询用户。
     *
     * 前端提交用户名demo
     * -> Spring Security调用查询工具
     * -> 查询工具找到demoUser
     * -> 取出demoUser中的密码哈希
     * -> 与前端提交的密码进行匹配
     *
     * 这一轮生成了两个对象
     * .build是创建了用户对象 创建好的对象要被变量demoUser保存
     * 然后最后返回一个内存用户管理器 并把demoUser放进去 临时内存 暂时还没有链接数据库 所以用临时内存
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        LoginUser demoUser = new LoginUser(
                1L,
                "demo",
                passwordEncoder.encode("123456"),
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Set.of(100L)
        );

        // 学习阶段仍然从内存查询；以后把这里替换为查询 user 表。
        return username -> {
            if (!demoUser.getUsername().equals(username)) {
                throw new UsernameNotFoundException("用户不存在");
            }
            return demoUser;
        };
    }

    /**
     * 3 组织上面两个工具完成整个认证
     * 认证管理器负责协调“查询用户”和“校验密码”。
     * 下一步登录接口会调用它进行真正的账号密码认证。
     *
     * 拿到了username = demo
     * -> 调用UserDetailsService查询demo
     * -> 查到demoUser
     * -> 取得demoUser 保存到密码哈希
     * -> 调用PasswordEncoder.matches(...)
     * -> 密码正确: 认证成功
     * -> 密码错误: 认证失败
     */
    @Bean
    public AuthenticationManager authenticationManager(
            // AuthenticationConfiguration是Spring Security自动准备的认证配置对象
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
