package com.cargoco.mapper;

import com.cargoco.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductImageMapper {
    List<ProductImage> findByProductId(Long productId);
    ProductImage findCoverByProductId(Long productId);
    int insert(ProductImage image);
    int batchInsert(@Param("list") List<ProductImage> images);
    int deleteByProductId(Long productId);
}
