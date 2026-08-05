package org.example.file_api.security;

import org.example.file_api.security.config.SecurityConfig;
import org.example.file_api.security.context.CurrentUserProvider;
import org.example.file_api.security.controller.AdminController;
import org.example.file_api.security.controller.AuthController;
import org.example.file_api.security.controller.CurrentUserController;
import org.example.file_api.security.controller.MethodSecurityController;
import org.example.file_api.security.controller.TenantController;
import org.example.file_api.controller.Test2Controller;
import org.example.file_api.security.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        Test2Controller.class,
        AdminController.class,
        MethodSecurityController.class,
        CurrentUserController.class,
        TenantController.class
})
@Import({
        SecurityConfig.class,
        TokenService.class,
        CurrentUserProvider.class
})
class SecurityFlowTest {

    private static final Pattern ACCESS_TOKEN_PATTERN =
            Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoginAndUseJwtToAccessProtectedEndpoint() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }
        String token = matcher.group(1);

        mockMvc.perform(get("/hi")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsInvalid() throws Exception {
        mockMvc.perform(get("/hi")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsMissing() throws Exception {
        mockMvc.perform(get("/hi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenUserDoesNotHaveAdminRole() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }

        mockMvc.perform(get("/admin/ping")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + matcher.group(1)
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("权限不足"));
    }

    @Test
    void shouldApplyPreAuthorizeBeforeCallingControllerMethod() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }

        mockMvc.perform(get("/method-security/admin")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + matcher.group(1)
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("权限不足"));
    }

    @Test
    void shouldReadCurrentUserIdAndUsernameFromSecurityContext() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }

        mockMvc.perform(get("/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + matcher.group(1)
                ))
                .andExpect(status().isOk())
                .andExpect(content().string("current user: 1 - demo"));
    }

    @Test
    void shouldUseVerifiedTenantIdInBusinessCode() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }

        mockMvc.perform(get("/tenant/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + matcher.group(1))
                        .header("tenant-id", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("tenant: 100, user: 1"));
    }

    @Test
    void shouldRejectTenantThatDoesNotBelongToCurrentUser() throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "demo",
                                    "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(loginResponse);
        if (!matcher.find()) {
            throw new AssertionError("登录响应中没有 accessToken");
        }

        mockMvc.perform(get("/tenant/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + matcher.group(1))
                        .header("tenant-id", "200"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("当前用户不属于该租户"));
    }
}
