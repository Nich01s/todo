package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.StatsResponse;
import com.todo.service.StatsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily")
    public Result<StatsResponse> daily(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statsService.getDailyStats(userId));
    }

    @GetMapping("/weekly")
    public Result<StatsResponse> weekly(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statsService.getWeeklyStats(userId));
    }
}
