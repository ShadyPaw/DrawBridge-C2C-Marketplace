package com.cargoco.mapper;

import com.cargoco.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReviewMapper {
    Review findById(Long id);
    List<Review> findByToUserId(Long toUserId);
    List<Review> findByOrderId(Long orderId);
    Review findByOrderIdAndFromUserId(@Param("orderId") Long orderId, @Param("fromUserId") Long fromUserId);
    int insert(Review review);
    int deleteById(Long id);
    Double avgRatingByToUserId(Long toUserId);
}
