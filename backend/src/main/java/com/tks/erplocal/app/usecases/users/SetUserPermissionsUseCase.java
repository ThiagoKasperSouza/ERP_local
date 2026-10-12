package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.permissions.model.PermissionsEnum;
import com.tks.erplocal.domain.users.exceptions.UserNotFoundException;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class SetUserPermissionsUseCase {
    private final UserRepositoryPort users;

    public SetUserPermissionsUseCase(UserRepositoryPort users) {
        this.users = users;
    }

    /** Substitui os grants do usuário; cada código precisa existir em PermissionsEnum (US1). */
    public User execute(UUID id, Set<String> codes, String grantedBy) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        Set<String> valid = new HashSet<>();
        if (codes != null) {
            for (String code : codes) {
                valid.add(PermissionsEnum.fromCode(code).getCode());
            }
        }
        user.setPermissions(valid);
        user.setPermissionsGrantedBy(grantedBy);
        return users.save(user);
    }
}
