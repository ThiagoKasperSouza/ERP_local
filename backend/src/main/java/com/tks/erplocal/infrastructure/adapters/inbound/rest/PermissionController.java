package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.app.usecases.permissions.ListPermissionsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    public record PermissionResponse(String code, String description) {}

    private final ListPermissionsUseCase listPermissionsUseCase;

    public PermissionController(ListPermissionsUseCase listPermissionsUseCase) {
        this.listPermissionsUseCase = listPermissionsUseCase;
    }

    @GetMapping
    public List<PermissionResponse> list() {
        return listPermissionsUseCase.execute().stream()
                .map(p -> new PermissionResponse(p.getCode(), p.getDescription()))
                .toList();
    }
}
