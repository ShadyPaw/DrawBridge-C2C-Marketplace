package com.cargoco.service;

import com.cargoco.common.PageResult;
import com.cargoco.entity.User;
import java.util.Map;

public interface UserService {
    Map<String, Object> login(String username, String password);
    User register(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User updateUser(User user);
    void updatePassword(Long id, String oldPassword, String newPassword);
    void updateStatus(Long id, Integer status);
    PageResult<User> getUserList(String keyword, Integer status, Integer role, Integer pageNum, Integer pageSize);
    Long countAll();
}
