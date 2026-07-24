package org.example.file_api.controller;

import org.example.file_api.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示 Controller/业务代码从 SecurityContext 获取当前用户。
 * 访问:
 * GET /me
 * Authorization: Bearer <JWT>
 * 返回:
 * current user: demo
 */
@RestController
@RequestMapping("/me")
public class CurrentUserController {

    //注入获取username的工具
    private final CurrentUserProvider currentUserProvider;

    public CurrentUserController(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String currentUser() {
        return "current user: "
                + currentUserProvider.getUserId()
                + " - "
                + currentUserProvider.getUsername();
    }
}
