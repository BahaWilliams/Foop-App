package com.food.FoopApp.auth_user.services;

import com.food.FoopApp.auth_user.domain.dto.UserDto;
import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.auth_user.repository.UserRepository;
import com.food.FoopApp.aws.AwsS3Services;
import com.food.FoopApp.email_notification.domain.dto.NotificationDto;
import com.food.FoopApp.email_notification.services.NotificationService;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AwsS3Services awsS3Services;

    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public Response<List<UserDto>> getAllUser() {
        log.info("get all users");
        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDto> userDto = modelMapper.map(userList, new TypeToken<List<UserDto>>(){}.getType());

        return Response.<List<UserDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All user retrieved")
                .data(userDto)
                .build();
    }

    @Override
    public Response<UserDto> getOwnAccountDetail() {
        log.info("own details");
        User user = getCurrentLoggedInUser();
        UserDto userDto = modelMapper.map(user, UserDto.class);

        return Response.<UserDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(userDto)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDto userDto) {
        log.info("update account");
        User user = getCurrentLoggedInUser();
        String profileUrl = user.getProfileUrl();
        MultipartFile multipartFile = userDto.getImageFile();

        if (multipartFile != null && !multipartFile.isEmpty()) {
            // Delete the old image from S3 if it exists
            if (profileUrl != null && !profileUrl.isEmpty()) {
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awsS3Services.delete("profile/" + keyName);

                log.info("Deleted old profile image from s3");
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
            URL newImageUrl = awsS3Services.uploadFile("profile/" + imageName, multipartFile);

            user.setProfileUrl(newImageUrl.toString());
        }

        if(userDto.getName() != null) user.setName(userDto.getName());
        if(userDto.getPhoneNumber() != null) user.setPhoneNumber(userDto.getPhoneNumber());
        if(userDto.getAddress() != null) user.setAddress(userDto.getAddress());
        if(userDto.getPassword() != null) user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            // Check if the new email is already taken
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account update successfully")
                .build();
    }

    @Override
    public Response<?> deActiveOwnAccount() {
        log.info("de-active account");
        User user = getCurrentLoggedInUser();

        user.setActive(false);
        userRepository.save(user);

        NotificationDto notificationDto = NotificationDto.builder()
                .recipient(user.getEmail())
                .subject("Account de-active")
                .body("Account have been de-active. If it was mistaken, contact support")
                .build();

        notificationService.sendEmail(notificationDto);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("de-active success")
                .build();
    }
}
