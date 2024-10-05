
package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.ERole;
import com.mbat.mbatapi.auth.entity.Role;
import com.mbat.mbatapi.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }
}
