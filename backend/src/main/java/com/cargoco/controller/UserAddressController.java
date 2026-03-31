package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.UserAddress;
import com.cargoco.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/list")
    public Result<List<UserAddress>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userAddressService.getByUserId(userId));
    }

    @PostMapping("/add")
    public Result<UserAddress> add(@RequestBody UserAddress address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        address.setUserId(userId);
        return Result.success("添加成功", userAddressService.add(address));
    }

    @PutMapping("/update")
    public Result<UserAddress> update(@RequestBody UserAddress address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        address.setUserId(userId);
        return Result.success("更新成功", userAddressService.update(address));
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userAddressService.delete(id);
        return Result.success("删除成功", null);
    }
}
