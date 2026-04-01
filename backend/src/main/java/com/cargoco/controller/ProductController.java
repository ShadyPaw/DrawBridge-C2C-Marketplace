package com.cargoco.controller;

import com.cargoco.common.PageResult;
import com.cargoco.common.Result;
import com.cargoco.entity.Product;
import com.cargoco.service.FavoriteService;
import com.cargoco.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/publish")
    public Result<Product> publish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Product product = new Product();
            product.setUserId(userId);
            product.setTitle((String) params.get("title"));
            product.setDescription((String) params.get("description"));
            product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
            product.setPrice(new BigDecimal(params.get("price").toString()));
            if (params.get("originalPrice") != null) {
                product.setOriginalPrice(new BigDecimal(params.get("originalPrice").toString()));
            }
            product.setQuality(params.get("quality") != null ? Integer.parseInt(params.get("quality").toString()) : 5);
            product.setLocation((String) params.get("location"));
            product.setTradeMode(params.get("tradeMode") != null ? Integer.parseInt(params.get("tradeMode").toString()) : 1);

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) params.get("imageUrls");

            Product published = productService.publish(product, imageUrls);
            return Result.success("发布成功，等待审核", published);
        } catch (Exception e) {
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer quality,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<Product> result = productService.getList(keyword, categoryId, 1, 1, userId, minPrice, maxPrice, quality, orderBy, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/search")
    public Result<PageResult<Product>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<Product> result = productService.getList(keyword, categoryId, 1, 1, null, minPrice, maxPrice, null, orderBy, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        try {
            Product product = productService.getDetail(id);
            return Result.success(product);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Product> update(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Product product = new Product();
            product.setId(Long.valueOf(params.get("id").toString()));
            product.setTitle((String) params.get("title"));
            product.setDescription((String) params.get("description"));
            if (params.get("categoryId") != null) product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
            if (params.get("price") != null) product.setPrice(new BigDecimal(params.get("price").toString()));
            if (params.get("originalPrice") != null) product.setOriginalPrice(new BigDecimal(params.get("originalPrice").toString()));
            if (params.get("quality") != null) product.setQuality(Integer.parseInt(params.get("quality").toString()));
            product.setLocation((String) params.get("location"));
            if (params.get("tradeMode") != null) product.setTradeMode(Integer.parseInt(params.get("tradeMode").toString()));

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) params.get("imageUrls");

            Product updated = productService.update(product, imageUrls);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            productService.delete(id, userId);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Product>> myProducts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Product> list = productService.getByUserId(userId);
        return Result.success(list);
    }
}
