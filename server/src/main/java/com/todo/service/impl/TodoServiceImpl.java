package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.common.BizException;
import com.todo.dto.TodoCreateRequest;
import com.todo.dto.TodoUpdateRequest;
import com.todo.entity.Todo;
import com.todo.mapper.TodoMapper;
import com.todo.service.TodoService;
import org.springframework.stereotype.Service;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    public IPage<Todo> query(Long userId, Integer completed, Integer priority, Long categoryId,
                              String keyword, String sort, int page, int size) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, userId);
        if (completed != null) wrapper.eq(Todo::getCompleted, completed);
        if (priority != null) wrapper.eq(Todo::getPriority, priority);
        if (categoryId != null) wrapper.eq(Todo::getCategoryId, categoryId);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Todo::getTitle, keyword);
        if ("due_date".equals(sort)) wrapper.orderByAsc(Todo::getDueDate);
        else if ("priority".equals(sort)) wrapper.orderByDesc(Todo::getPriority);
        else wrapper.orderByDesc(Todo::getCreatedAt);
        return todoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Todo create(Long userId, TodoCreateRequest request) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        todo.setDueDate(request.getDueDate());
        todo.setCategoryId(request.getCategoryId());
        todoMapper.insert(todo);
        return todo;
    }

    @Override
    public Todo update(Long userId, Long id, TodoUpdateRequest request) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new BizException(404, "待办不存在");
        }
        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getPriority() != null) todo.setPriority(request.getPriority());
        if (request.getDueDate() != null) todo.setDueDate(request.getDueDate());
        if (request.getCategoryId() != null) todo.setCategoryId(request.getCategoryId());
        todoMapper.updateById(todo);
        return todo;
    }

    @Override
    public void delete(Long userId, Long id) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new BizException(404, "待办不存在");
        }
        todoMapper.deleteById(id);
    }

    @Override
    public Todo toggle(Long userId, Long id) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new BizException(404, "待办不存在");
        }
        todo.setCompleted(todo.getCompleted() == 1 ? 0 : 1);
        todoMapper.updateById(todo);
        return todo;
    }
}
