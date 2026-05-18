package com.todo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TodoUpdateRequest {
    private String title;
    private String description;
    private Integer priority;
    private LocalDate dueDate;
    private Long categoryId;
}
