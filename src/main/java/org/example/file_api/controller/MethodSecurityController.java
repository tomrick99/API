package org.example.file_api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示方法级权限：请求通过 URL 规则后，调用方法前再次检查角色。
 */
@RestController
@RequestMapping("/method-security")
public class MethodSecurityController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminOnly() {
        return "method security admin";
    }
}
