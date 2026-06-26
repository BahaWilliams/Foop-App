package com.food.FoopApp.review.services;

import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.auth_user.services.UserService;
import com.food.FoopApp.enums.OrderStatus;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.menu.domain.entity.Menu;
import com.food.FoopApp.menu.repository.MenuRepository;
import com.food.FoopApp.order.domain.entity.Order;
import com.food.FoopApp.order.repository.OrderItemRepository;
import com.food.FoopApp.order.repository.OrderRepository;
import com.food.FoopApp.response.Response;
import com.food.FoopApp.review.domain.dto.ReviewDto;
import com.food.FoopApp.review.domain.entity.Review;
import com.food.FoopApp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    public final ReviewRepository reviewRepository;
    public final OrderRepository orderRepository;
    public final OrderItemRepository orderItemRepository;
    public final ModelMapper modelMapper;
    public final UserService userService;
    public final MenuRepository menuRepository;

    @Override
    @Transactional
    public Response<ReviewDto> createReview(ReviewDto reviewDto) {
        log.info("create review");

        User user = userService.getCurrentLoggedInUser();

        if(reviewDto.getOrderId() == null || reviewDto.getMenuId() == null){
            throw new BadRequestException("Order ID and Menu ID is required");
        }

        Menu menu = menuRepository.findById(reviewDto.getMenuId()).orElseThrow(() -> new NotFoundException("Menu not found"));
        Order order = orderRepository.findById(reviewDto.getOrderId()).orElseThrow(() -> new NotFoundException("Order not found"));

        if(!order.getUser().getId().equals(user.getId())){
            throw new BadRequestException("Order isn't for this user");
        }

        if(order.getOrderStatus() != OrderStatus.DELIVERED){
            throw new BadRequestException("Review can only be for ordered items");
        }

        boolean itemInOrder = orderItemRepository.existsByOrderIdAndMenuId(reviewDto.getOrderId(), reviewDto.getMenuId());

        if(!itemInOrder){
            throw new BadRequestException("Review and Order id was not suppose to be there");
        }

        if (reviewRepository.existsByUserIdAndMenuIdAndOrderId(user.getId(), reviewDto.getOrderId(), reviewDto.getMenuId())){
            throw new BadRequestException("You already review this order ");
        }

        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .orderId(reviewDto.getOrderId())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        ReviewDto responseDto = modelMapper.map(savedReview, ReviewDto.class);
        responseDto.setUserName(user.getName());
        responseDto.setMenuName(menu.getName());

        return Response.<ReviewDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review added successfully")
                .data(responseDto)
                .build();
    }

    @Override
    public Response<List<ReviewDto>> getReviewFromMenu(Long menuId) {
        log.info("Inside getReviewsForMenu()");

        List<Review> reviews = reviewRepository.findByMenuIdOrderByIdDesc(menuId);

        List<ReviewDto> reviewDTOs = reviews.stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .toList();

        return Response.<List<ReviewDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Reviews retrieved successfully")
                .data(reviewDTOs)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {
        log.info("Inside getAverageRating()");

        Double averageRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Average rating retrieved successfully")
                .data(averageRating != null ? averageRating : 0.0)
                .build();
    }
}
