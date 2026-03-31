package com.cargoco.mapper;

import com.cargoco.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {
    Category findById(Long id);
    List<Category> findAll();
    List<Category> findByParentId(Long parentId);
    int insert(Category category);
    int update(Category category);
    int deleteById(Long id);
}
