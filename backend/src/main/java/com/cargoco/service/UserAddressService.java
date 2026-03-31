package com.cargoco.service;

import com.cargoco.entity.UserAddress;
import java.util.List;

public interface UserAddressService {
    List<UserAddress> getByUserId(Long userId);
    UserAddress getById(Long id);
    UserAddress add(UserAddress address);
    UserAddress update(UserAddress address);
    void delete(Long id);
}
