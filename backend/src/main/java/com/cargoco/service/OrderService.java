package com.cargoco.service;

import com.cargoco.entity.OrderInfo;
import com.cargoco.common.PageResult;
import java.util.List;

public interface OrderService {
    OrderInfo createOrder(OrderInfo order);
    OrderInfo getDetail(Long id);
    void pay(Long id, Long buyerId);
    void ship(Long id, Long sellerId);
    void receive(Long id, Long buyerId);
    void cancel(Long id, Long userId, String reason);
    PageResult<OrderInfo> getBuyerOrders(Long buyerId, Integer orderStatus, Integer pageNum, Integer pageSize);
    PageResult<OrderInfo> getSellerOrders(Long sellerId, Integer orderStatus, Integer pageNum, Integer pageSize);
    PageResult<OrderInfo> getAllOrders(Integer orderStatus, String keyword, Integer pageNum, Integer pageSize);
    Long countAll();
    Long countByStatus(Integer orderStatus);
}
