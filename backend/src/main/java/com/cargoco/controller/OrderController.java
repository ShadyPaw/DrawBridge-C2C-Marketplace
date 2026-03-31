package com.cargoco.controller;

import com.cargoco.common.PageResult;
import com.cargoco.common.Result;
import com.cargoco.entity.OrderInfo;
import com.cargoco.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<OrderInfo> create(@RequestBody OrderInfo order, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            order.setBuyerId(userId);
            OrderInfo created = orderService.createOrder(order);
            return Result.success("下单成功", created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<OrderInfo> detail(@PathVariable Long id) {
        OrderInfo order = orderService.getDetail(id);
        return Result.success(order);
    }

    @PutMapping("/pay/{id}")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            orderService.pay(id, userId);
            return Result.success("支付成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/ship/{id}")
    public Result<?> ship(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            orderService.ship(id, userId);
            return Result.success("发货成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/receive/{id}")
    public Result<?> receive(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            orderService.receive(id, userId);
            return Result.success("确认收货成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            orderService.cancel(id, userId, params.get("reason"));
            return Result.success("取消成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/buyer")
    public Result<PageResult<OrderInfo>> buyerOrders(
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getBuyerOrders(userId, orderStatus, pageNum, pageSize));
    }

    @GetMapping("/seller")
    public Result<PageResult<OrderInfo>> sellerOrders(
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getSellerOrders(userId, orderStatus, pageNum, pageSize));
    }
}
