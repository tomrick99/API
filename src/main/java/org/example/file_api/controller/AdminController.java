package org.example.file_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用来演示“已经登录，但角色权限不足”的 403 场景。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/ping")
    public String ping() {
        return "admin pong";
    }
}
