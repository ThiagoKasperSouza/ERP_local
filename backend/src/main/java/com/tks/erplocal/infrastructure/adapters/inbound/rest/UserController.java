package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.app.usecases.users.CreateUserUseCase;
import com.tks.erplocal.app.usecases.users.DeactivateUserUseCase;
import com.tks.erplocal.app.usecases.users.ListUsersUseCase;
import com.tks.erplocal.app.usecases.users.SetUserPermissionsUseCase;
import com.tks.erplocal.app.usecases.users.UpdateUserUseCase;
import com.tks.erplocal.domain.users.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ListUsersUseCase listUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final SetUserPermissionsUseCase setUserPermissionsUseCase;

    public UserController(ListUsersUseCase listUsersUseCase,
                          CreateUserUseCase createUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeactivateUserUseCase deactivateUserUseCase,
                          SetUserPermissionsUseCase setUserPermissionsUseCase) {
        this.listUsersUseCase = listUsersUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.setUserPermissionsUseCase = setUserPermissionsUseCase;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        ListUsersUseCase.Page result = listUsersUseCase.execute(page, size);
        List<UserResponse> items = result.items().stream().map(UserResponse::from).toList();
        return Map.of("page", result.page(), "size", result.size(),
                "total", result.total(), "items", items);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest body) {
        Role role = body.role() == null ? Role.USER : Role.fromString(body.role());
        return UserResponse.from(createUserUseCase.execute(body.name(), body.email(), body.password(), role));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody UpdateUserRequest body) {
        Role role = body.role() == null ? null : Role.fromString(body.role());
        return UserResponse.from(updateUserUseCase.execute(id, body.name(), role, body.active(), body.password()));
    }

    @DeleteMapping("/{id}")
    public UserResponse deactivate(@PathVariable UUID id) {
        return UserResponse.from(deactivateUserUseCase.execute(id));
    }

    @PutMapping("/{id}/permissions")
    public UserResponse setPermissions(@PathVariable UUID id, @RequestBody Set<String> codes) {
        // grantedBy null: ainda sem identidade do chamador (login futuro preenche)
        return UserResponse.from(setUserPermissionsUseCase.execute(id, codes, null));
    }
}
