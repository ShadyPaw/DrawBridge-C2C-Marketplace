package com.cargoco.service;

import com.cargoco.entity.Favorite;
import java.util.List;

public interface FavoriteService {
    boolean toggle(Long userId, Long productId);
    List<Favorite> getByUserId(Long userId);
    boolean isFavorited(Long userId, Long productId);
}
