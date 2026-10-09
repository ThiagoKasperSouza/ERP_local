package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import com.tks.erplocal.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
public class VaultPermissionInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepositoryPort users;

    public VaultPermissionInterceptor(JwtService jwtService, UserRepositoryPort users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        RequirePermission required = requiredPermission(handler);
        if (required == null) {
            return true;
        }
        Optional<JwtService.Principal> principal = principalFrom(request);
        if (principal.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return false;
        }
        Optional<User> user = users.findById(principal.get().userId());
        if (user.isEmpty() || !user.get().isActive()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown or inactive user");
            return false;
        }
        if (user.get().getRole() == Role.ADMIN) {
            return true;
        }
        if (required.adminOnly() || !user.get().getPermissions().contains(required.value())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing permission: " + required.value());
            return false;
        }
        request.setAttribute("currentUserId", user.get().getId());
        return true;
    }

    private RequirePermission requiredPermission(Object handler) {
        if (handler instanceof HandlerMethod method) {
            RequirePermission onMethod = method.getMethodAnnotation(RequirePermission.class);
            if (onMethod != null) {
                return onMethod;
            }
            return method.getBeanType().getAnnotation(RequirePermission.class);
        }
        return null;
    }

    private Optional<JwtService.Principal> principalFrom(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.toLowerCase().startsWith("bearer ")) {
            return Optional.empty();
        }
        return jwtService.verify(authorization.substring(7).trim());
    }
}
