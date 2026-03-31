package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Message;
import com.cargoco.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result<Message> send(@RequestBody Message message, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        message.setUserId(userId);
        Message sent = messageService.send(message);
        return Result.success("留言成功", sent);
    }

    @GetMapping("/product/{productId}")
    public Result<List<Message>> getByProductId(@PathVariable Long productId) {
        return Result.success(messageService.getByProductId(productId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            messageService.delete(id, userId);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
