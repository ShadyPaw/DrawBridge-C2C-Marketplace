package com.cargoco.mapper;

import com.cargoco.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BannerMapper {
    Banner findById(Long id);
    List<Banner> findActive();
    List<Banner> findAll();
    int insert(Banner banner);
    int update(Banner banner);
    int deleteById(Long id);
}
