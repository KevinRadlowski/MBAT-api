
package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.ERole;
import com.mbat.mbatapi.entity.Role;
import com.mbat.mbatapi.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable("name") ERole name) {
        Optional<Role> role = roleService.findByName(name);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.save(role);
    }
}
