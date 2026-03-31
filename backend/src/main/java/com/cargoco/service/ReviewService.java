package com.cargoco.service;

import com.cargoco.entity.Review;
import java.util.List;

public interface ReviewService {
    Review addReview(Review review);
    List<Review> getByToUserId(Long toUserId);
    List<Review> getByOrderId(Long orderId);
}
