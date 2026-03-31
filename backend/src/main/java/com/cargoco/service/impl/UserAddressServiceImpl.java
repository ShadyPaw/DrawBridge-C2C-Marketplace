package com.cargoco.service.impl;

import com.cargoco.entity.UserAddress;
import com.cargoco.mapper.UserAddressMapper;
import com.cargoco.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getByUserId(Long userId) {
        return userAddressMapper.findByUserId(userId);
    }

    @Override
    public UserAddress getById(Long id) {
        return userAddressMapper.findById(id);
    }

    @Override
    @Transactional
    public UserAddress add(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.clearDefault(address.getUserId());
        }
        userAddressMapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public UserAddress update(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.clearDefault(address.getUserId());
        }
        userAddressMapper.update(address);
        return userAddressMapper.findById(address.getId());
    }

    @Override
    public void delete(Long id) {
        userAddressMapper.deleteById(id);
    }
}
