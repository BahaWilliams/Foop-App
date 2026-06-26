package com.food.FoopApp.auth_user.services;

import com.food.FoopApp.auth_user.domain.dto.UserDto;
import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.response.Response;

import java.util.List;

public interface UserService {
    public User getCurrentLoggedInUser();
    public Response<List<UserDto>> getAllUser();
    public Response<UserDto> getOwnAccountDetail();
    public Response<?> updateOwnAccount(UserDto userDto);
    public Response<?> deActiveOwnAccount();
}
