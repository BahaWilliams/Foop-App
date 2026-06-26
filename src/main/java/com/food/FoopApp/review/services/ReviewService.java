package com.food.FoopApp.review.services;

import com.food.FoopApp.response.Response;
import com.food.FoopApp.review.domain.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    Response<ReviewDto> createReview(ReviewDto reviewDto);
    Response<List<ReviewDto>> getReviewFromMenu(Long menuId);
    Response<Double> getAverageRating(Long menuId);
}
