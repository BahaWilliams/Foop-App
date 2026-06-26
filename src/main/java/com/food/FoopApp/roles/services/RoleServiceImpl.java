package com.food.FoopApp.roles.services;

import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.response.Response;
import com.food.FoopApp.roles.domain.dto.RoleDto;
import com.food.FoopApp.roles.domain.entity.Role;
import com.food.FoopApp.roles.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<RoleDto> createRole(RoleDto roleDto) {
        Role role = modelMapper.map(roleDto, Role.class);
        Role saveRole = roleRepository.save(role);

        return Response.<RoleDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(modelMapper.map(saveRole, RoleDto.class))
                .build();
    }

    @Override
    public Response<RoleDto> updateRole(RoleDto roleDto) {
        Role existingRole = roleRepository.findById(roleDto.getId())
                .orElseThrow(() ->
                        new NotFoundException("Role not found")
                );

        if(roleRepository.findByName(roleDto.getName()).isPresent()){
            throw new BadRequestException("Role with name already exist");
        }

        existingRole.setName(roleDto.getName());

        Role updatedRole = roleRepository.save(existingRole);

        return Response.<RoleDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role updated successfully")
                .data(modelMapper.map(updatedRole, RoleDto.class))
                .build();
    }

    @Override
    public Response<?> deleteAllRole(Long id) {
        if(!roleRepository.existsById(id)){
            throw new NotFoundException("Role was not found");
        }

        roleRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role delete successfully")
                .build();
    }

    @Override
    public Response<List<RoleDto>> getAllRole() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> roleDTOS = roles.stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();


        return Response.<List<RoleDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roleDTOS)
                .build();
    }
}
