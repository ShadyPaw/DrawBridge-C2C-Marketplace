package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Favorite;
import com.cargoco.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/toggle/{productId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isFavorited = favoriteService.toggle(userId, productId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorited", isFavorited);
        return Result.success(isFavorited ? "收藏成功" : "已取消收藏", data);
    }

    @GetMapping("/list")
    public Result<List<Favorite>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(favoriteService.getByUserId(userId));
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Object>> check(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorited", favoriteService.isFavorited(userId, productId));
        return Result.success(data);
    }
}
