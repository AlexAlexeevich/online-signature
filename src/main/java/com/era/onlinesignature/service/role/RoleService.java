package com.era.onlinesignature.service.role;

import com.era.onlinesignature.entity.Role;
import com.era.onlinesignature.entity.enums.ERole;

import java.util.Optional;


public interface RoleService {
    Optional<Role> findByName(ERole name);
}
