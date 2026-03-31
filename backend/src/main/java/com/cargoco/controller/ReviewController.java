package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Review;
import com.cargoco.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public Result<Review> add(@RequestBody Review review, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            review.setFromUserId(userId);
            Review added = reviewService.addReview(review);
            return Result.success("评价成功", added);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> getByUserId(@PathVariable Long userId) {
        return Result.success(reviewService.getByToUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<Review>> getByOrderId(@PathVariable Long orderId) {
        return Result.success(reviewService.getByOrderId(orderId));
    }
}
