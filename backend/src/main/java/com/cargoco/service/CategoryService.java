package com.cargoco.service;

import com.cargoco.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    List<Category> getTree();
    Category getById(Long id);
    Category add(Category category);
    Category update(Category category);
    void delete(Long id);
}
