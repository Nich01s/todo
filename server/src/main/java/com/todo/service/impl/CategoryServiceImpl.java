package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.common.BizException;
import com.todo.dto.CategoryRequest;
import com.todo.entity.Category;
import com.todo.mapper.CategoryMapper;
import com.todo.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> listByUser(Long userId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getUserId, userId)
        );
    }

    @Override
    public Category create(Long userId, CategoryRequest request) {
        Category c = new Category();
        c.setUserId(userId);
        c.setName(request.getName());
        c.setColor(request.getColor());
        categoryMapper.insert(c);
        return c;
    }

    @Override
    public Category update(Long userId, Long id, CategoryRequest request) {
        Category c = categoryMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BizException(404, "分类不存在");
        }
        c.setName(request.getName());
        c.setColor(request.getColor());
        categoryMapper.updateById(c);
        return c;
    }

    @Override
    public void delete(Long userId, Long id) {
        Category c = categoryMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BizException(404, "分类不存在");
        }
        categoryMapper.deleteById(id);
    }
}
