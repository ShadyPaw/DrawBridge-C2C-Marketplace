package com.cargoco.mapper;

import com.cargoco.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteMapper {
    Favorite findById(Long id);
    Favorite findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    List<Favorite> findByUserId(Long userId);
    int insert(Favorite favorite);
    int deleteById(Long id);
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
