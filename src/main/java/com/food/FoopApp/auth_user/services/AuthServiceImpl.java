package com.food.FoopApp.auth_user.services;

import com.food.FoopApp.auth_user.domain.dto.LoginRequest;
import com.food.FoopApp.auth_user.domain.dto.LoginResponse;
import com.food.FoopApp.auth_user.domain.dto.RegistrationRequest;
import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.auth_user.repository.UserRepository;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.response.Response;
import com.food.FoopApp.roles.domain.entity.Role;
import com.food.FoopApp.roles.repository.RoleRepository;
import com.food.FoopApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("register");

        if(userRepository.existsByEmail(registrationRequest.getEmail())){
            throw new BadRequestException("Email already exist");
        }

        //find all roles
        List<Role> userRoles;

        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){
            userRoles = registrationRequest.getRoles()
                    .stream()
                    .map((roleName) -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() ->
                                    new NotFoundException("Role with name: " + roleName + " not found")
                            )
                    ).toList();
        }else {
            //default as customer
            Role defaultRole = roleRepository.findByName("CUSTOMER").orElseThrow(() ->
                    new NotFoundException("Customer role was not found")
            );

            userRoles = List.of(defaultRole);
        }

        //build user object
        User userToSave = User
                .builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createAt(LocalDateTime.now())
                .build();

        userRepository.save(userToSave);
        log.info("user successful register");

        return Response
                .builder()
                .statusCode(HttpStatus.OK.value())
                .message("User register successfully")
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        log.info("login");

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() ->
                new NotFoundException("Invalid Email")
        );

        if(!user.isActive()){
            throw new NotFoundException("Accounts not active, please contact customer support");
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid password");
        }

        String token = jwtUtils.generateToken(loginRequest.getEmail());

        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleNames);

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successfully")
                .data(loginResponse)
                .build();
    }
}
