package com.cargoco.mapper;

import com.cargoco.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderInfoMapper {
    OrderInfo findById(Long id);
    OrderInfo findByOrderNo(String orderNo);
    OrderInfo findDetailById(Long id);
    int insert(OrderInfo order);
    int update(OrderInfo order);
    int updateStatus(@Param("id") Long id, @Param("orderStatus") Integer orderStatus);
    List<OrderInfo> findByBuyerId(@Param("buyerId") Long buyerId, @Param("orderStatus") Integer orderStatus);
    List<OrderInfo> findBySellerId(@Param("sellerId") Long sellerId, @Param("orderStatus") Integer orderStatus);
    Long countByUserId(@Param("userId") Long userId);
    List<OrderInfo> findAll(@Param("orderStatus") Integer orderStatus, @Param("keyword") String keyword);
    Long countAll();
    Long countByStatus(@Param("orderStatus") Integer orderStatus);
}
