package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Category;
import com.cargoco.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getAll());
    }

    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.getTree());
    }
}
