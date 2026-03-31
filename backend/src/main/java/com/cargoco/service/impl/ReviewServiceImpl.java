package com.cargoco.service.impl;

import com.cargoco.entity.CreditRecord;
import com.cargoco.entity.Review;
import com.cargoco.entity.User;
import com.cargoco.mapper.CreditRecordMapper;
import com.cargoco.mapper.ReviewMapper;
import com.cargoco.mapper.UserMapper;
import com.cargoco.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditRecordMapper creditRecordMapper;

    @Override
    @Transactional
    public Review addReview(Review review) {
        // 检查是否已评价
        Review existing = reviewMapper.findByOrderIdAndFromUserId(review.getOrderId(), review.getFromUserId());
        if (existing != null) {
            throw new RuntimeException("您已对该订单进行过评价");
        }

        reviewMapper.insert(review);

        // 更新被评价人信用积分
        User toUser = userMapper.findById(review.getToUserId());
        if (toUser != null) {
            int changeValue = 0;
            int changeType;
            String remark;
            if (review.getRating() == 3) { // 好评
                changeValue = 5;
                changeType = 2;
                remark = "获得好评 +5";
            } else if (review.getRating() == 1) { // 差评
                changeValue = -10;
                changeType = 3;
                remark = "获得差评 -10";
            } else { // 中评
                changeValue = 0;
                changeType = 2;
                remark = "获得中评";
            }

            if (changeValue != 0) {
                int beforeScore = toUser.getCreditScore();
                int afterScore = Math.max(0, beforeScore + changeValue);
                // 计算等级
                int level = afterScore >= 200 ? 3 : (afterScore >= 100 ? 2 : 1);
                userMapper.updateCreditScore(toUser.getId(), afterScore, level);

                // 记录信用变动
                CreditRecord record = new CreditRecord();
                record.setUserId(toUser.getId());
                record.setChangeType(changeType);
                record.setChangeValue(changeValue);
                record.setBeforeScore(beforeScore);
                record.setAfterScore(afterScore);
                record.setRelatedId(review.getOrderId());
                record.setRemark(remark);
                creditRecordMapper.insert(record);
            }
        }

        return review;
    }

    @Override
    public List<Review> getByToUserId(Long toUserId) {
        return reviewMapper.findByToUserId(toUserId);
    }

    @Override
    public List<Review> getByOrderId(Long orderId) {
        return reviewMapper.findByOrderId(orderId);
    }
}
