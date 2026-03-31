package com.cargoco.service.impl;

import com.cargoco.entity.Category;
import com.cargoco.mapper.CategoryMapper;
import com.cargoco.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAll() {
        return categoryMapper.findAll();
    }

    @Override
    public List<Category> getTree() {
        List<Category> all = categoryMapper.findAll();
        // 构建树形结构
        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        roots.forEach(root -> root.setChildren(getChildren(root.getId(), all)));
        return roots;
    }

    private List<Category> getChildren(Long parentId, List<Category> all) {
        List<Category> children = all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .collect(Collectors.toList());
        children.forEach(child -> child.setChildren(getChildren(child.getId(), all)));
        return children;
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.findById(id);
    }

    @Override
    public Category add(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        categoryMapper.update(category);
        return categoryMapper.findById(category.getId());
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
