package com.food.FoopApp.auth_user.controller;

import com.food.FoopApp.auth_user.domain.dto.LoginRequest;
import com.food.FoopApp.auth_user.domain.dto.LoginResponse;
import com.food.FoopApp.auth_user.domain.dto.RegistrationRequest;
import com.food.FoopApp.auth_user.services.AuthService;
import com.food.FoopApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> registerUser(@RequestBody @Valid RegistrationRequest registrationRequest){
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> loginUser(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
