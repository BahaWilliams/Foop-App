package com.food.FoopApp.category.services;

import com.food.FoopApp.category.domain.dto.CategoryDto;
import com.food.FoopApp.category.domain.entity.Category;
import com.food.FoopApp.category.repository.CategoryRepository;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CategoryDto> addCategory(CategoryDto categoryDto) {
        log.info("add category");
        Category category = modelMapper.map(categoryDto, Category.class);
        categoryRepository.save(category);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success add category")
                .build();
    }

    @Override
    public Response<CategoryDto> updateCategory(CategoryDto categoryDto) {
        log.info("update category");
        Category category = categoryRepository.findById(categoryDto.getId()).orElseThrow(() ->
                new NotFoundException("Category not found"));

        if(categoryDto.getName() != null && !categoryDto.getName().isEmpty()) category.setName(categoryDto.getName());
        if(categoryDto.getDescription() != null) category.setDescription(categoryDto.getDescription());

        categoryRepository.save(category);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success update category")
                .build();
    }

    @Override
    public Response<CategoryDto> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Category not found"));

        CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success get category id")
                .data(categoryDto)
                .build();
    }

    @Override
    public Response<List<CategoryDto>> getAllCategory() {
        log.info("get all category");

        List<Category> categories = categoryRepository.findAll();

        List<CategoryDto> categoryDto = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .toList();

        return Response.<List<CategoryDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All categories retrieved successfully")
                .data(categoryDto)
                .build();
    }

    @Override
    public Response<?> deleteCategory(Long id) {
        log.info("delete category");

        if(!categoryRepository.existsById(id)) throw  new NotFoundException("Category not found");
        categoryRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success delete category id")
                .build();
    }
}
