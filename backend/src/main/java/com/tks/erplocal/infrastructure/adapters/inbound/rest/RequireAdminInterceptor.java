package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guarda temporária até existir login/JWT: considera admin quem envia
 * {@code X-User-Role: admin}. Qualquer outro valor (ou ausência) → 403.
 */
@Component
public class RequireAdminInterceptor implements HandlerInterceptor {

    static final String ROLE_HEADER = "X-User-Role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if ("admin".equalsIgnoreCase(request.getHeader(ROLE_HEADER))) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
        return false;
    }
}
