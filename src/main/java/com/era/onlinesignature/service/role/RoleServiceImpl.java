package com.era.onlinesignature.service.role;

import com.era.onlinesignature.entity.Role;
import com.era.onlinesignature.entity.enums.ERole;
import com.era.onlinesignature.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleServiceImpl implements RoleService {

    private final RoleRepository userRepository;

    @Override
    public Optional<Role> findByName(ERole name) {
        return userRepository.findByName(name);
    }
}
