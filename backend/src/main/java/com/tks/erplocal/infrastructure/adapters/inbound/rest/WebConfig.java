package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequireAdminInterceptor requireAdminInterceptor;
    private final VaultPermissionInterceptor vaultPermissionInterceptor;

    public WebConfig(RequireAdminInterceptor requireAdminInterceptor,
                     VaultPermissionInterceptor vaultPermissionInterceptor) {
        this.requireAdminInterceptor = requireAdminInterceptor;
        this.vaultPermissionInterceptor = vaultPermissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requireAdminInterceptor).addPathPatterns("/api/users/**");
        registry.addInterceptor(vaultPermissionInterceptor).addPathPatterns("/api/vault/**");
    }
}
