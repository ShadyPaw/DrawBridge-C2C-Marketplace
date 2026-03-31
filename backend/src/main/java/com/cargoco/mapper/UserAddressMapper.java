package com.cargoco.mapper;

import com.cargoco.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserAddressMapper {
    UserAddress findById(Long id);
    List<UserAddress> findByUserId(Long userId);
    int insert(UserAddress address);
    int update(UserAddress address);
    int deleteById(Long id);
    int clearDefault(Long userId);
}
