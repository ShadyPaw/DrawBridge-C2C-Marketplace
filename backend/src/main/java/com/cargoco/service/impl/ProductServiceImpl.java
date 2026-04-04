package com.cargoco.service.impl;

import com.cargoco.common.PageResult;
import com.cargoco.entity.Product;
import com.cargoco.entity.ProductImage;
import com.cargoco.mapper.ProductImageMapper;
import com.cargoco.mapper.ProductMapper;
import com.cargoco.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Override
    @Transactional
    public Product publish(Product product, List<String> imageUrls) {
        product.setProductStatus(1);
        product.setAuditStatus(1);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage img = new ProductImage();
                img.setProductId(product.getId());
                img.setImageUrl(imageUrls.get(i));
                img.setIsCover(i == 0 ? 1 : 0);
                img.setSortOrder(i);
                images.add(img);
            }
            productImageMapper.batchInsert(images);
        }
        return product;
    }

    @Override
    public Product getDetail(Long id) {
        Product product = productMapper.findDetailById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        List<ProductImage> images = productImageMapper.findByProductId(id);
        product.setImages(images);
        productMapper.incrementViewCount(id);
        return product;
    }

    @Override
    @Transactional
    public Product update(Product product, List<String> imageUrls) {
        product.setAuditStatus(1);
        productMapper.update(product);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            productImageMapper.deleteByProductId(product.getId());
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage img = new ProductImage();
                img.setProductId(product.getId());
                img.setImageUrl(imageUrls.get(i));
                img.setIsCover(i == 0 ? 1 : 0);
                img.setSortOrder(i);
                images.add(img);
            }
            productImageMapper.batchInsert(images);
        }
        return productMapper.findDetailById(product.getId());
    }

    @Override
    public void delete(Long id, Long userId) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此商品");
        }
        productMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
        if (status == 1) {
            productMapper.updateAudit(id, 1, "重新上架自动通过", null);
        }
    }

    @Override
    public void audit(Long id, Integer auditStatus, String auditRemark, Long auditUserId) {
        productMapper.updateAudit(id, auditStatus, auditRemark, auditUserId);
    }

    @Override
    public PageResult<Product> getList(String keyword, Long categoryId, Integer productStatus, Integer auditStatus,
                                       Long userId, BigDecimal minPrice, BigDecimal maxPrice, Integer quality,
                                       String orderBy, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.findList(keyword, categoryId, productStatus, auditStatus, userId, minPrice, maxPrice, quality, orderBy);
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public List<Product> getByUserId(Long userId) {
        return productMapper.findByUserId(userId);
    }

    @Override
    public Long countAll() {
        return productMapper.countAll();
    }

    @Override
    public Long countByStatus(Integer auditStatus) {
        return productMapper.countByStatus(auditStatus);
    }
}