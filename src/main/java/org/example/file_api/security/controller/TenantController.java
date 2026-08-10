package org.example.file_api.security.controller;

import org.example.file_api.security.context.CurrentUserProvider;
import org.example.file_api.security.context.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示业务代码从 TenantContext 取得当前已验证的租户。
 */
@RestController
@RequestMapping("/tenant")
public class TenantController {

    private final CurrentUserProvider currentUserProvider;

    public TenantController(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public String currentTenant() {
        return "tenant: " + TenantContext.getTenantId()
                + ", user: " + currentUserProvider.getUserId();
    }
}
