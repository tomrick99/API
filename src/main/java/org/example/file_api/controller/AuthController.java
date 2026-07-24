package org.example.file_api.controller;

import jakarta.validation.Valid;
import org.example.file_api.dto.LoginRequest;
import org.example.file_api.dto.LoginResponse;
import org.example.file_api.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API文件
 * 处理登录相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 注入认证总管
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    // Spring创建AuthController的时候 会从容器里面找到准备好的AuthenticationManager然后传进来
    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        // 登录方法收到并验证LoginRequest后 执行下面的创建request 存储着一个等待验证的账号密码对象
        var authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                request.getUsername(),
                request.getPassword()
        );

        // 真实的认证步骤
        try {
            Authentication authenticationResult =
                    authenticationManager.authenticate(authenticationRequest);

            // 认证成功后才生成 JWT，密码错误时不会执行到这里。
            String token = tokenService.generateToken(authenticationResult.getName());

            return new LoginResponse(
                    token,
                    tokenService.getExpirationSeconds()
            );
        } catch (AuthenticationException exception) {   // 用户名不存在或者密码错误
            throw new ResponseStatusException(  //捕获异常 前端返回401 Unauthorized
                    HttpStatus.UNAUTHORIZED,
                    "用户名或密码错误"
            );
        }
    }
}
