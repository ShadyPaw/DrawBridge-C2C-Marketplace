package com.cargoco.service.impl;

import com.cargoco.entity.Favorite;
import com.cargoco.mapper.FavoriteMapper;
import com.cargoco.mapper.ProductMapper;
import com.cargoco.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public boolean toggle(Long userId, Long productId) {
        Favorite existing = favoriteMapper.findByUserIdAndProductId(userId, productId);
        if (existing != null) {
            favoriteMapper.deleteByUserIdAndProductId(userId, productId);
            productMapper.updateFavoriteCount(productId, -1);
            return false; // 取消收藏
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteMapper.insert(favorite);
            productMapper.updateFavoriteCount(productId, 1);
            return true; // 已收藏
        }
    }

    @Override
    public List<Favorite> getByUserId(Long userId) {
        return favoriteMapper.findByUserId(userId);
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.findByUserIdAndProductId(userId, productId) != null;
    }
}
