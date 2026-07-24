package org.example.file_api.dto;

/**
 * 登录成功后返回给前端的数据。
 */
public class LoginResponse {

    // 正真的JWT
    private final String accessToken;

    // 前端携带时用的Bearer
    private final String tokenType;

    // expiresIn 7200秒 就是两小时
    private final long expiresIn;

    public LoginResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
