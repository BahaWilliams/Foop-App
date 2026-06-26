package com.food.FoopApp.roles.services;

import com.food.FoopApp.response.Response;
import com.food.FoopApp.roles.domain.dto.RoleDto;

import java.util.List;

public interface RoleService {
    public Response<RoleDto> createRole(RoleDto roleDto);
    public Response<RoleDto> updateRole(RoleDto roleDto);
    public Response<?> deleteAllRole(Long id);
    public Response<List<RoleDto>> getAllRole();
}
