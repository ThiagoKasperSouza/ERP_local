package com.tks.erplocal.app.usecases.permissions;

import org.springframework.stereotype.Service;
import java.util.List;

import com.tks.erplocal.domain.permissions.model.PermissionsEnum;


@Service
public class ListPermissionsUseCase {
  public List<PermissionsEnum> execute(){ return List.of(PermissionsEnum.values()); }
}
