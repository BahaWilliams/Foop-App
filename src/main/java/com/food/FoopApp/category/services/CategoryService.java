package com.food.FoopApp.category.services;

import com.food.FoopApp.category.domain.dto.CategoryDto;
import com.food.FoopApp.response.Response;

import java.util.List;

public interface CategoryService {
    Response<CategoryDto> addCategory(CategoryDto categoryDto);
    Response<CategoryDto> updateCategory(CategoryDto categoryDto);
    Response<CategoryDto> getCategoryById(Long id);
    Response<List<CategoryDto>> getAllCategory();
    Response<?> deleteCategory(Long id);

}
