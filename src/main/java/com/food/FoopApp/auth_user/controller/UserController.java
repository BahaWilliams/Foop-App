package com.food.FoopApp.auth_user.controller;

import com.food.FoopApp.auth_user.domain.dto.UserDto;
import com.food.FoopApp.auth_user.services.UserService;
import com.food.FoopApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")//Admin API access all user
    public ResponseEntity<Response<List<UserDto>>> getAllUser(){
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<?>> updateUserAccount(@ModelAttribute @Valid UserDto userDto,
                                                         @RequestPart(value = "multipartFile",
                                                                 required = false) MultipartFile multipartFile
                                                         ){
        userDto.setImageFile(multipartFile);
        return ResponseEntity.ok(userService.updateOwnAccount(userDto));
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Response<?>> deactivateOwnAccount() {
        return ResponseEntity.ok(userService.deActiveOwnAccount());
    }

    @GetMapping("/account")
    public ResponseEntity<Response<UserDto>> getOwnAccountDetails() {
        return ResponseEntity.ok(userService.getOwnAccountDetail());
    }
}
