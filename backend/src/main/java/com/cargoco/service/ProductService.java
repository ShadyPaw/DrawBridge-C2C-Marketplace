package com.cargoco.service;

import com.cargoco.common.PageResult;
import com.cargoco.entity.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Product publish(Product product, List<String> imageUrls);
    Product getDetail(Long id);
    Product update(Product product, List<String> imageUrls);
    void delete(Long id, Long userId);
    void updateStatus(Long id, Integer status);
    void audit(Long id, Integer auditStatus, String auditRemark, Long auditUserId);
    PageResult<Product> getList(String keyword, Long categoryId, Integer productStatus, Integer auditStatus,
                                Long userId, BigDecimal minPrice, BigDecimal maxPrice, Integer quality,
                                String orderBy, Integer pageNum, Integer pageSize);
    List<Product> getByUserId(Long userId);
    Long countAll();
    Long countByStatus(Integer auditStatus);
}
