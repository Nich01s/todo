package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.dto.StatsResponse;
import com.todo.entity.Todo;
import com.todo.mapper.TodoMapper;
import com.todo.service.StatsService;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class StatsServiceImpl implements StatsService {

    private final TodoMapper todoMapper;

    public StatsServiceImpl(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    public StatsResponse getDailyStats(Long userId) {
        LocalDate today = LocalDate.now();
        return calcStats(userId, today, today);
    }

    @Override
    public StatsResponse getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        return calcStats(userId, weekStart, today);
    }

    private StatsResponse calcStats(Long userId, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, userId)
                .ge(Todo::getDueDate, from)
                .le(Todo::getDueDate, to);
        long total = todoMapper.selectCount(wrapper);
        wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, userId)
                .eq(Todo::getCompleted, 1)
                .ge(Todo::getDueDate, from)
                .le(Todo::getDueDate, to);
        long completed = todoMapper.selectCount(wrapper);
        double rate = total > 0 ? (double) completed / total : 0.0;
        return new StatsResponse(completed, total, rate);
    }
}
