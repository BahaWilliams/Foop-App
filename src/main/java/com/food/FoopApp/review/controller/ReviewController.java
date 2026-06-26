package com.food.FoopApp.review.controller;

import com.food.FoopApp.response.Response;
import com.food.FoopApp.review.domain.dto.ReviewDto;
import com.food.FoopApp.review.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<ReviewDto>> createReview(@RequestBody @Valid ReviewDto reviewDto){
        return ResponseEntity.ok(reviewService.createReview(reviewDto));
    }

    @GetMapping("/menu-item/{menuId}")
    public ResponseEntity<Response<List<ReviewDto>>> getMenuReview(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getReviewFromMenu(menuId));
    }

    @GetMapping("/menu-item/average/{menuId}")
    public ResponseEntity<Response<Double>> getAverageRating(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getAverageRating(menuId));
    }
}
