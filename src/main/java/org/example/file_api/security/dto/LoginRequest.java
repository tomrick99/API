package org.example.file_api.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 接收前端调用 POST /auth/login 时提交的 JSON 数据。
 * 这里只负责装数据和做最基础的非空校验，不负责判断账号密码是否正确。
 */
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
