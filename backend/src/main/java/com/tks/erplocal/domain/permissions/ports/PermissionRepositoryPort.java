package com.tks.erplocal.domain.permissions.ports;

import com.tks.erplocal.domain.permissions.model.PermissionsEnum;
import java.util.List;

public interface PermissionRepositoryPort {
  List<PermissionsEnum> findAll();
  boolean existsByCode(String code);
  void save(PermissionsEnum p);
}
