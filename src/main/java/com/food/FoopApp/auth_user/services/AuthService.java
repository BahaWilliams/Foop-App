package com.food.FoopApp.auth_user.services;

import com.food.FoopApp.auth_user.domain.dto.LoginRequest;
import com.food.FoopApp.auth_user.domain.dto.LoginResponse;
import com.food.FoopApp.auth_user.domain.dto.RegistrationRequest;
import com.food.FoopApp.response.Response;

public interface AuthService {
    public Response<?> register(RegistrationRequest registrationRequest);
    public Response<LoginResponse> login(LoginRequest loginRequest);

}
