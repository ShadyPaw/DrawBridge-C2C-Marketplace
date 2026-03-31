package com.cargoco.controller;

import com.cargoco.common.PageResult;
import com.cargoco.common.Result;
import com.cargoco.entity.User;
import com.cargoco.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            Map<String, Object> data = userService.login(params.get("username"), params.get("password"));
            return Result.success("登录成功", data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User registered = userService.register(user);
            return Result.success("注册成功", registered);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @GetMapping("/detail/{id}")
    public Result<User> getDetail(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        User updated = userService.updateUser(user);
        return Result.success("更新成功", updated);
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
            return Result.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
