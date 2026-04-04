package com.cargoco.mapper;

import com.cargoco.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Long id);
    User findByUsername(String username);
    User findByPhone(String phone);
    int insert(User user);
    int update(User user);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int updateCreditScore(@Param("id") Long id, @Param("creditScore") Integer creditScore, @Param("userLevel") Integer userLevel);
    int updateCreditScoreOnly(@Param("id") Long id, @Param("creditScore") Integer creditScore);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(Long id);
    List<User> findList(@Param("keyword") String keyword, @Param("status") Integer status, @Param("role") Integer role);
    Long countAll();
}
