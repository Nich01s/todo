package com.todo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.todo.dto.TodoCreateRequest;
import com.todo.dto.TodoUpdateRequest;
import com.todo.entity.Todo;

public interface TodoService {
    IPage<Todo> query(Long userId, Integer completed, Integer priority, Long categoryId,
                      String keyword, String sort, int page, int size);
    Todo create(Long userId, TodoCreateRequest request);
    Todo update(Long userId, Long id, TodoUpdateRequest request);
    void delete(Long userId, Long id);
    Todo toggle(Long userId, Long id);
}
