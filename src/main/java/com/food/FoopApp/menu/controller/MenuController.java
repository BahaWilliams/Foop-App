package com.food.FoopApp.menu.controller;

import com.food.FoopApp.auth_user.domain.dto.UserDto;
import com.food.FoopApp.menu.domain.dto.MenuDto;
import com.food.FoopApp.menu.services.MenuService;
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
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDto>> createMenu(@ModelAttribute @Valid MenuDto menuDto,
                                                        @RequestPart(value = "imageFile", required = true)MultipartFile multipartFile){
        menuDto.setImageFile(multipartFile);
        return ResponseEntity.ok(menuService.createMenu(menuDto));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDto>> updateMenu(@ModelAttribute @Valid MenuDto menuDto,
                                                        @RequestPart(value = "imageFile", required = false)MultipartFile multipartFile){
        menuDto.setImageFile(multipartFile);
        return ResponseEntity.ok(menuService.updateMenu(menuDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response<MenuDto>> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>> deleteMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.deleteMenu(id));
    }

    @GetMapping
    public ResponseEntity<Response<List<MenuDto>>> getMenu(@RequestParam(required = false) Long categoryId,
                                                           @RequestParam(required = false) String search) {
        return ResponseEntity.ok(menuService.getMenu(categoryId, search));
    }

}
