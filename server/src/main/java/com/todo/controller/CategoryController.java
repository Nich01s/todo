package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.CategoryRequest;
import com.todo.entity.Category;
import com.todo.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.listByUser(userId));
    }

    @PostMapping
    public Result<Category> create(Authentication auth, @Valid @RequestBody CategoryRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Category> update(Authentication auth, @PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        categoryService.delete(userId, id);
        return Result.success();
    }
}
