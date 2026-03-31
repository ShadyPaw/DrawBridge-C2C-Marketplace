package com.cargoco.service.impl;

import com.cargoco.common.PageResult;
import com.cargoco.entity.OrderInfo;
import com.cargoco.entity.Product;
import com.cargoco.mapper.OrderInfoMapper;
import com.cargoco.mapper.ProductMapper;
import com.cargoco.service.OrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public OrderInfo createOrder(OrderInfo order) {
        Product product = productMapper.findById(order.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getProductStatus() != 1) {
            throw new RuntimeException("商品已下架或已售出");
        }
        if (product.getUserId().equals(order.getBuyerId())) {
            throw new RuntimeException("不能购买自己发布的商品");
        }
        // 生成订单编号
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNo(orderNo);
        order.setSellerId(product.getUserId());
        order.setPrice(product.getPrice());
        order.setTradeMode(product.getTradeMode());
        order.setOrderStatus(0); // 待付款
        orderInfoMapper.insert(order);
        return orderInfoMapper.findDetailById(order.getId());
    }

    @Override
    public OrderInfo getDetail(Long id) {
        return orderInfoMapper.findDetailById(id);
    }

    @Override
    @Transactional
    public void pay(Long id, Long buyerId) {
        OrderInfo order = orderInfoMapper.findById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new RuntimeException("无权操作此订单");
        if (order.getOrderStatus() != 0) throw new RuntimeException("订单状态不正确");
        order.setOrderStatus(1); // 待发货
        order.setPayTime(new Date());
        orderInfoMapper.update(order);
    }

    @Override
    @Transactional
    public void ship(Long id, Long sellerId) {
        OrderInfo order = orderInfoMapper.findById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new RuntimeException("无权操作此订单");
        if (order.getOrderStatus() != 1) throw new RuntimeException("订单状态不正确");
        order.setOrderStatus(2); // 待收货
        order.setShipTime(new Date());
        orderInfoMapper.update(order);
    }

    @Override
    @Transactional
    public void receive(Long id, Long buyerId) {
        OrderInfo order = orderInfoMapper.findById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new RuntimeException("无权操作此订单");
        if (order.getOrderStatus() != 2) throw new RuntimeException("订单状态不正确");
        order.setOrderStatus(3); // 已完成
        order.setReceiveTime(new Date());
        orderInfoMapper.update(order);
        // 将商品状态改为已售出
        productMapper.updateStatus(order.getProductId(), 3);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId, String reason) {
        OrderInfo order = orderInfoMapper.findById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getOrderStatus() >= 3) throw new RuntimeException("订单已完成或已取消，无法取消");
        order.setOrderStatus(4); // 已取消
        order.setCancelTime(new Date());
        order.setCancelReason(reason);
        orderInfoMapper.update(order);
    }

    @Override
    public PageResult<OrderInfo> getBuyerOrders(Long buyerId, Integer orderStatus, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderInfo> list = orderInfoMapper.findByBuyerId(buyerId, orderStatus);
        PageInfo<OrderInfo> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public PageResult<OrderInfo> getSellerOrders(Long sellerId, Integer orderStatus, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderInfo> list = orderInfoMapper.findBySellerId(sellerId, orderStatus);
        PageInfo<OrderInfo> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public PageResult<OrderInfo> getAllOrders(Integer orderStatus, String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderInfo> list = orderInfoMapper.findAll(orderStatus, keyword);
        PageInfo<OrderInfo> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public Long countAll() {
        return orderInfoMapper.countAll();
    }

    @Override
    public Long countByStatus(Integer orderStatus) {
        return orderInfoMapper.countByStatus(orderStatus);
    }
}
