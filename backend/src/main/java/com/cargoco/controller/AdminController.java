package com.cargoco.controller;

import com.cargoco.common.PageResult;
import com.cargoco.common.Result;
import com.cargoco.entity.*;
import com.cargoco.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DataInitService dataInitService;

    // ==================== 数据初始化 ====================
    @PostMapping("/init-data")
    public Result<?> initData() {
        dataInitService.initCommunityData();
        return Result.success("社区生态数据初始化成功", null);
    }

    // ==================== 统计概览 ====================
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.countAll());
        data.put("productCount", productService.countAll());
        data.put("orderCount", orderService.countAll());
        data.put("pendingAudit", productService.countByStatus(0));
        data.put("pendingReport", reportService.countByStatus(0));
        data.put("completedOrders", orderService.countByStatus(3));
        return Result.success(data);
    }

    // ==================== 用户管理 ====================
    @GetMapping("/user/list")
    public Result<PageResult<User>> userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.getUserList(keyword, status, null, pageNum, pageSize));
    }

    @PutMapping("/user/status/{id}")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success(status == 0 ? "已禁用" : "已启用", null);
    }

    // ==================== 商品审核 ====================
    @GetMapping("/product/list")
    public Result<PageResult<Product>> productList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.getList(keyword, null, null, auditStatus, null, null, null, null, null, pageNum, pageSize));
    }

    @PutMapping("/product/audit/{id}")
    public Result<?> auditProduct(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer auditStatus = Integer.parseInt(params.get("auditStatus").toString());
        String auditRemark = (String) params.get("auditRemark");
        productService.audit(id, auditStatus, auditRemark, adminId);
        return Result.success(auditStatus == 1 ? "审核通过" : "审核拒绝", null);
    }

    @DeleteMapping("/product/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.updateStatus(id, 2); // 下架
        return Result.success("已下架", null);
    }

    // ==================== 订单管理 ====================
    @GetMapping("/order/list")
    public Result<PageResult<OrderInfo>> orderList(
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.getAllOrders(orderStatus, keyword, pageNum, pageSize));
    }

    // ==================== 举报管理 ====================
    @GetMapping("/report/list")
    public Result<PageResult<Report>> reportList(
            @RequestParam(required = false) Integer handleStatus,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.getList(handleStatus, targetType, pageNum, pageSize));
    }

    @PutMapping("/report/handle/{id}")
    public Result<Report> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer handleStatus = Integer.parseInt(params.get("handleStatus").toString());
        String handleResult = (String) params.get("handleResult");
        Report handled = reportService.handle(id, handleStatus, handleResult, adminId);
        return Result.success("处理成功", handled);
    }

    // ==================== 通知管理 ====================
    @GetMapping("/notice/list")
    public Result<PageResult<Notice>> noticeList(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.getAll(type, pageNum, pageSize));
    }

    @PostMapping("/notice/add")
    public Result<Notice> addNotice(@RequestBody Notice notice, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        notice.setSenderId(adminId);
        return Result.success("发布成功", noticeService.add(notice));
    }

    @PutMapping("/notice/update")
    public Result<Notice> updateNotice(@RequestBody Notice notice) {
        return Result.success("更新成功", noticeService.update(notice));
    }

    @DeleteMapping("/notice/delete/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("删除成功", null);
    }

    // ==================== 分类管理 ====================
    @PostMapping("/category/add")
    public Result<Category> addCategory(@RequestBody Category category) {
        return Result.success("添加成功", categoryService.add(category));
    }

    @PutMapping("/category/update")
    public Result<Category> updateCategory(@RequestBody Category category) {
        return Result.success("更新成功", categoryService.update(category));
    }

    @DeleteMapping("/category/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
