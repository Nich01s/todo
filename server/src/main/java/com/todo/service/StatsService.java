package com.todo.service;

import com.todo.dto.StatsResponse;

public interface StatsService {
    StatsResponse getDailyStats(Long userId);
    StatsResponse getWeeklyStats(Long userId);
}
