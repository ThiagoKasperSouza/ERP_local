package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Guarda de admin: exige {@code Authorization: Bearer <jwt>} com claim role=ADMIN. */
@Component
public class RequireAdminInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public RequireAdminInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            String token = authorization.substring(7).trim();
            var principal = jwtService.verify(token);
            if (principal.isPresent() && principal.get().role() == Role.ADMIN) {
                return true;
            }
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
        return false;
    }
}
