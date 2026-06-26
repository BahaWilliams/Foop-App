package com.food.FoopApp.menu.services;

import com.food.FoopApp.aws.AwsS3Services;
import com.food.FoopApp.category.domain.entity.Category;
import com.food.FoopApp.category.repository.CategoryRepository;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.menu.domain.dto.MenuDto;
import com.food.FoopApp.menu.domain.entity.Menu;
import com.food.FoopApp.menu.repository.MenuRepository;
import com.food.FoopApp.response.Response;
import com.food.FoopApp.review.domain.dto.ReviewDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final AwsS3Services awsS3Services;

    @Override
    public Response<MenuDto> createMenu(MenuDto menuDto) {
        log.info("create menu");

        Category category = categoryRepository.findById(menuDto.getCategoryId()).orElseThrow(() -> new NotFoundException("Category was not found"));
        String imageUrl = null;
        MultipartFile multipartFile = menuDto.getImageFile();

        if(multipartFile == null || multipartFile.isEmpty()){
            throw new BadRequestException("Menu image is require");
        }

        String imageName = UUID.randomUUID() + "_" + multipartFile.getName();
        URL s3Url = awsS3Services.uploadFile("menus/" + imageName, multipartFile);
        imageUrl = s3Url.toString();

        Menu menu = Menu.builder()
                .name(menuDto.getName())
                .category(category)
                .imageUrl(imageUrl)
                .description(menuDto.getDescription())
                .price(menuDto.getPrice())
                .build();

        Menu saveMenu = menuRepository.save(menu);

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu created successfully")
                .data(modelMapper.map(saveMenu, MenuDto.class))
                .build();
    }

    @Override
    public Response<MenuDto> updateMenu(MenuDto menuDto) {
        log.info("update menu");

        Menu existingMenu =menuRepository.findById(menuDto.getId()).orElseThrow(() -> new NotFoundException("menu was not found"));
        Category category = categoryRepository.findById(menuDto.getCategoryId()).orElseThrow(() -> new NotFoundException("category was not found"));

        String imageUrl = existingMenu.getImageUrl();
        MultipartFile multipartFile = menuDto.getImageFile();

        if (multipartFile != null && !multipartFile.isEmpty()) {
            // Delete the old image from S3 if it exists
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                awsS3Services.delete("menus/" + keyName);

                log.info("Deleted old menu image from s3");
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
            URL newImageUrl = awsS3Services.uploadFile("menus/" + imageName, multipartFile);
            imageUrl = newImageUrl.toString();
        }

        if(menuDto.getName() != null && !menuDto.getName().isBlank()) existingMenu.setName(menuDto.getName());
        if(menuDto.getDescription() != null && !menuDto.getDescription().isBlank()) existingMenu.setDescription(menuDto.getName());
        if(menuDto.getPrice() != null) existingMenu.setPrice(menuDto.getPrice());

        existingMenu.setImageUrl(imageUrl);
        existingMenu.setCategory(category);

        Menu saveMenu = menuRepository.save(existingMenu);

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully")
                .data(modelMapper.map(saveMenu, MenuDto.class))
                .build();
    }

    @Override
    public Response<MenuDto> getMenuById(Long id) {
        log.info("get menu by id");

        Menu existingMenu =menuRepository.findById(id).orElseThrow(() -> new NotFoundException("menu was not found"));
        MenuDto menuDto = modelMapper.map(existingMenu, MenuDto.class);

        //Sort the reviews descending order
        if(menuDto.getReviews() != null){
             menuDto.getReviews().sort(Comparator.comparing(ReviewDto::getId).reversed());
        }

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu reviewed successfully")
                .data(menuDto)
                .build();
    }

    @Override
    public Response<?> deleteMenu(Long id) {
        log.info("delete menu by id");

        Menu menuToDelete = menuRepository.findById(id).orElseThrow(() -> new NotFoundException("menu was not found"));

        //delete image from s3 if it exists
        String imageUrl = menuToDelete.getImageUrl();

        if(imageUrl != null && !imageUrl.isEmpty()){
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            awsS3Services.delete("menus/" + keyName);

            log.info("delete menu image");
        }

        menuRepository.deleteById(id);

        return Response.<MenuDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu image deleted successfully")
                .build();
    }

    @Override
    public Response<List<MenuDto>> getMenu(Long id, String search) {
        log.info("get menu");

        Specification<Menu> specification = buildSpecification(id, search);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<Menu> menuList = menuRepository.findAll(specification, sort);
        List<MenuDto> menuDto = menuList.stream()
                .map(menu -> modelMapper.map(menu, MenuDto.class))
                .toList();

        return Response.<List<MenuDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menus retrieved")
                .data(menuDto)
                .build();
    }

    private Specification<Menu> buildSpecification(Long categoryId, String search){
        return (root, query, criteriaBuilder) -> {
         List<Predicate> predicates = new ArrayList<>();

         if(categoryId != null){
             predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
         }

         if(search != null && !search.isEmpty()){
             String searchTerm = "%" + search.toLowerCase() + "%";
             predicates.add(criteriaBuilder.or(criteriaBuilder
                     .like(criteriaBuilder
                             .lower(root
                                     .get("name")), searchTerm), criteriaBuilder
                     .like(criteriaBuilder
                             .lower(root.get("description")), searchTerm
                     )
             ));
         }

         return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
