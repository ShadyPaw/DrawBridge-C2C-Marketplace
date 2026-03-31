package com.cargoco.mapper;

import com.cargoco.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    Product findById(Long id);
    Product findDetailById(Long id);
    int insert(Product product);
    int update(Product product);
    int deleteById(Long id);
    int updateStatus(@Param("id") Long id, @Param("productStatus") Integer productStatus);
    int updateAudit(@Param("id") Long id, @Param("auditStatus") Integer auditStatus,
                    @Param("auditRemark") String auditRemark, @Param("auditUserId") Long auditUserId);
    int incrementViewCount(Long id);
    int updateFavoriteCount(@Param("id") Long id, @Param("delta") Integer delta);
    List<Product> findList(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                           @Param("productStatus") Integer productStatus, @Param("auditStatus") Integer auditStatus,
                           @Param("userId") Long userId, @Param("minPrice") java.math.BigDecimal minPrice,
                           @Param("maxPrice") java.math.BigDecimal maxPrice, @Param("quality") Integer quality,
                           @Param("orderBy") String orderBy);
    List<Product> findByUserId(Long userId);
    Long countAll();
    Long countByStatus(@Param("auditStatus") Integer auditStatus);
}
