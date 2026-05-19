package com.todo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.todo.common.Result;
import com.todo.dto.TodoCreateRequest;
import com.todo.dto.TodoUpdateRequest;
import com.todo.entity.Todo;
import com.todo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public Result<IPage<Todo>> list(
            Authentication auth,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long category_id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(todoService.query(userId, status, priority, category_id, keyword, sort, page, size));
    }

    @PostMapping
    public Result<Todo> create(Authentication auth, @Valid @RequestBody TodoCreateRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(todoService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Todo> update(Authentication auth, @PathVariable Long id,
                                @Valid @RequestBody TodoUpdateRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(todoService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        todoService.delete(userId, id);
        return Result.success();
    }

    @PatchMapping("/{id}/toggle")
    public Result<Todo> toggle(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(todoService.toggle(userId, id));
    }
}
