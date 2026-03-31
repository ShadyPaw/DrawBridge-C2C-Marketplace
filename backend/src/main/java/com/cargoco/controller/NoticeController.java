package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Notice;
import com.cargoco.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public Result<List<Notice>> systemNotices() {
        return Result.success(noticeService.getSystemNotices());
    }

    @GetMapping("/my")
    public Result<List<Notice>> myNotices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(noticeService.getByTargetUserId(userId));
    }

    @PutMapping("/read/{id}")
    public Result<?> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return Result.success();
    }

    @PutMapping("/readAll")
    public Result<?> markAllRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        noticeService.markAllRead(userId);
        return Result.success();
    }

    @GetMapping("/unreadCount")
    public Result<Map<String, Object>> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();
        data.put("count", noticeService.countUnread(userId));
        return Result.success(data);
    }
}
