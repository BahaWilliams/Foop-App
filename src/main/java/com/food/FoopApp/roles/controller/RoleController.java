package com.food.FoopApp.roles.controller;

import com.food.FoopApp.response.Response;
import com.food.FoopApp.roles.domain.dto.RoleDto;
import com.food.FoopApp.roles.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Response<RoleDto>> createRole(@RequestBody @Valid RoleDto roleDto){
        return ResponseEntity.ok(roleService.createRole(roleDto));
    }

    @PutMapping
    public ResponseEntity<Response<RoleDto>> updateRole(@RequestBody @Valid RoleDto roleDto){
        return ResponseEntity.ok(roleService.updateRole(roleDto));
    }

    @GetMapping
    public ResponseEntity<Response<List<RoleDto>>> getAllRole(){
        return ResponseEntity.ok(roleService.getAllRole());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable Long id){
        return ResponseEntity.ok(roleService.deleteAllRole(id));
    }
}
