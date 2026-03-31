package com.cargoco.service;

import com.cargoco.entity.Message;
import java.util.List;

public interface MessageService {
    Message send(Message message);
    List<Message> getByProductId(Long productId);
    void delete(Long id, Long userId);
}
