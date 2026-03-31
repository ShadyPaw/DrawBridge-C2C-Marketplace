package com.cargoco.service.impl;

import com.cargoco.entity.Message;
import com.cargoco.mapper.MessageMapper;
import com.cargoco.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Message send(Message message) {
        if (message.getParentId() == null) {
            message.setParentId(0L);
        }
        messageMapper.insert(message);
        return messageMapper.findById(message.getId());
    }

    @Override
    public List<Message> getByProductId(Long productId) {
        // 获取顶级留言
        List<Message> topMessages = messageMapper.findTopLevelByProductId(productId);
        // 获取每条留言的回复
        topMessages.forEach(msg -> {
            List<Message> replies = messageMapper.findReplies(msg.getId());
            msg.setReplies(replies);
        });
        return topMessages;
    }

    @Override
    public void delete(Long id, Long userId) {
        Message message = messageMapper.findById(id);
        if (message == null) {
            throw new RuntimeException("留言不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此留言");
        }
        messageMapper.deleteById(id);
    }
}
