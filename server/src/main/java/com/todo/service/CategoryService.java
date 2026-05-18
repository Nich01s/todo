package com.todo.service;

import com.todo.dto.CategoryRequest;
import com.todo.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listByUser(Long userId);
    Category create(Long userId, CategoryRequest request);
    Category update(Long userId, Long id, CategoryRequest request);
    void delete(Long userId, Long id);
}
