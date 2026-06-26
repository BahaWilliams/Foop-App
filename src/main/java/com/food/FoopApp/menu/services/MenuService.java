package com.food.FoopApp.menu.services;

import com.food.FoopApp.menu.domain.dto.MenuDto;
import com.food.FoopApp.response.Response;

import java.util.List;
import java.util.Optional;

public interface MenuService {
    Response<MenuDto> createMenu(MenuDto menuDto);
    Response<MenuDto> updateMenu(MenuDto menuDto);
    Response<MenuDto> getMenuById(Long id);
    Response<?> deleteMenu(Long id);
    Response<List<MenuDto>> getMenu(Long categoryId, String search);
}
