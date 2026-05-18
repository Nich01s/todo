package com.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("categories")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String color;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
