# 个人待办工具 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个基于 Vue 3 + Spring Boot 的个人待办仪表盘，包含日历视图（农历+节日）、天气组件、任务统计环形图和暗色/亮色主题切换。

**Architecture:** Nginx 托管 Vue 静态文件并反向代理 `/api/*` 到 Spring Boot 8080 端口。前端直接调用和风天气 API。后端 Spring Security + JWT 双 Token 认证，MyBatis-Plus 操作 MySQL。前后端分离开发，Vite dev server 开发时代理 API 请求。

**Tech Stack:** Vue 3 (Composition API) + Vite + Pinia + Axios / Spring Boot 3 + Spring Security + MyBatis-Plus + MySQL + JJWT

---

## 项目文件结构

```
Todo2/
├── server/                           # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/todo/
│       │   ├── TodoApplication.java
│       │   ├── config/               # SecurityConfig, WebConfig, MyBatisPlusConfig
│       │   ├── entity/               # User, Todo, Category, RefreshToken
│       │   ├── mapper/               # MyBatis-Plus Mapper
│       │   ├── dto/                  # 请求/响应 DTO
│       │   ├── service/impl/         # Service 层
│       │   ├── controller/           # REST Controller
│       │   ├── security/             # JwtTokenProvider, JwtAuthFilter
│       │   ├── common/               # Result, GlobalExceptionHandler
│       │   └── util/                 # FileUploadUtil
│       └── resources/
│           ├── application.yml
│           └── db/schema.sql
├── client/                           # Vue 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── stores/                   # auth.js, theme.js, weather.js
│       ├── api/                      # axios 实例 + 各模块 API
│       ├── views/                    # LoginView, RegisterView, DashboardView
│       ├── components/
│       │   ├── AppHeader.vue
│       │   ├── calendar/             # CalendarPanel, CalendarGrid, DayCell, MagnifierPopup
│       │   ├── weather/              # WeatherWidget, CitySearchDialog
│       │   ├── stats/                # StatsCard, RingChart, TaskDetailList
│       │   └── todo/                 # QuickAddBar, AddTodoDialog
│       └── styles/                   # variables.css, theme.css
└── docs/superpowers/specs/2026-05-18-personal-todo-design.md
```

---

## Phase 1: 后端基础搭建

### Task 1: 创建 Spring Boot 项目骨架

**Files:**
- Create: `server/pom.xml`
- Create: `server/src/main/java/com/todo/TodoApplication.java`
- Create: `server/src/main/resources/application.yml`

- [ ] **Step 1: 编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    <groupId>com.todo</groupId>
    <artifactId>todo-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>todo-server</name>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.5</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 编写 TodoApplication.java**

```java
package com.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
```

- [ ] **Step 3: 编写 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/todo_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 5MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

app:
  jwt:
    secret: your-jwt-secret-key-must-be-at-least-256-bits-long-please-change-me
    access-expiration: 900000
    refresh-expiration: 604800000
  upload:
    avatar-dir: uploads/avatars
```

- [ ] **Step 4: 验证构建**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 2: 数据库初始化

**Files:**
- Create: `server/src/main/resources/db/schema.sql`

- [ ] **Step 1: 编写 schema.sql**

```sql
CREATE DATABASE IF NOT EXISTS todo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE todo_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS todos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    completed TINYINT(1) NOT NULL DEFAULT 0,
    priority TINYINT NOT NULL DEFAULT 1 COMMENT '0=low, 1=medium, 2=high',
    due_date DATE NULL,
    category_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_completed_duedate (user_id, completed, due_date),
    INDEX idx_user_category (user_id, category_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_user_expires (user_id, expires_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 执行 schema.sql 创建数据库和表**

```bash
mysql -u root -p < server/src/main/resources/db/schema.sql
```

Expected: 数据库 todo_db 和 4 张表创建成功。

---

### Task 3: 实体类和 Mapper

**Files:**
- Create: `server/src/main/java/com/todo/entity/User.java`
- Create: `server/src/main/java/com/todo/entity/Category.java`
- Create: `server/src/main/java/com/todo/entity/Todo.java`
- Create: `server/src/main/java/com/todo/entity/RefreshToken.java`
- Create: `server/src/main/java/com/todo/mapper/UserMapper.java`
- Create: `server/src/main/java/com/todo/mapper/CategoryMapper.java`
- Create: `server/src/main/java/com/todo/mapper/TodoMapper.java`
- Create: `server/src/main/java/com/todo/mapper/RefreshTokenMapper.java`

- [ ] **Step 1: 编写 User.java**

```java
package com.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String avatar;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 编写 Category.java**

```java
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
```

- [ ] **Step 3: 编写 Todo.java**

```java
package com.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("todos")
public class Todo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer completed;  // 0 or 1
    private Integer priority;   // 0=low, 1=medium, 2=high
    private LocalDate dueDate;
    private Long categoryId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: 编写 RefreshToken.java**

```java
package com.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("refresh_tokens")
public class RefreshToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 编写 Mapper 接口**

```java
// UserMapper.java
package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

```java
// CategoryMapper.java
package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
```

```java
// TodoMapper.java
package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}
```

```java
// RefreshTokenMapper.java
package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
```

- [ ] **Step 6: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 4: 通用类和配置

**Files:**
- Create: `server/src/main/java/com/todo/common/Result.java`
- Create: `server/src/main/java/com/todo/common/BizException.java`
- Create: `server/src/main/java/com/todo/common/GlobalExceptionHandler.java`
- Create: `server/src/main/java/com/todo/config/MyBatisPlusConfig.java`
- Create: `server/src/main/java/com/todo/config/WebConfig.java`

- [ ] **Step 1: 编写 Result.java**

```java
package com.todo.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
```

- [ ] **Step 2: 编写 BizException.java**

```java
package com.todo.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 3: 编写 GlobalExceptionHandler.java**

```java
package com.todo.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("validation error");
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "Internal server error: " + e.getMessage());
    }
}
```

- [ ] **Step 4: 编写 MyBatisPlusConfig.java**

```java
package com.todo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 5: 编写 WebConfig.java**

```java
package com.todo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = new File(avatarDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path + "/");
    }
}
```

- [ ] **Step 6: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

## Phase 2: 后端认证

### Task 5: JWT 工具类

**Files:**
- Create: `server/src/main/java/com/todo/security/JwtTokenProvider.java`

- [ ] **Step 1: 编写 JwtTokenProvider.java**

```java
package com.todo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(
                Jwts.parser().verifyWith(key).build()
                        .parseSignedClaims(token).getPayload().getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 6: Spring Security 配置

**Files:**
- Create: `server/src/main/java/com/todo/security/UserDetailsServiceImpl.java`
- Create: `server/src/main/java/com/todo/security/JwtAuthFilter.java`
- Create: `server/src/main/java/com/todo/config/SecurityConfig.java`

- [ ] **Step 1: 编写 UserDetailsServiceImpl.java**

```java
package com.todo.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
```

- [ ] **Step 2: 编写 JwtAuthFilter.java**

```java
package com.todo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    userDetailsService.loadUserByUsername(/* we need a way to get username from userId */ "")
            );
            // For simplicity, we'll set the userId as the principal
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: 重新设计 JwtAuthFilter——不再依赖 UserDetailsService 查库**

JWT 本身自包含 subject(userId) 和 username claim。过滤器只需从 Token 解析即可，无需查数据库：

```java
package com.todo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

用此版本覆盖 Step 2。

- [ ] **Step 4: 编写 SecurityConfig.java**

```java
package com.todo.config;

import com.todo.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

- [ ] **Step 5: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 7: 认证 API（注册/登录/刷新/登出）

**Files:**
- Create: `server/src/main/java/com/todo/dto/LoginRequest.java`
- Create: `server/src/main/java/com/todo/dto/RegisterRequest.java`
- Create: `server/src/main/java/com/todo/dto/RefreshRequest.java`
- Create: `server/src/main/java/com/todo/dto/LoginResponse.java`
- Create: `server/src/main/java/com/todo/service/AuthService.java`
- Create: `server/src/main/java/com/todo/service/impl/AuthServiceImpl.java`
- Create: `server/src/main/java/com/todo/controller/AuthController.java`

- [ ] **Step 1: 编写 DTO**

```java
// LoginRequest.java
package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

```java
// RegisterRequest.java
package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50位")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度为6-50位")
    private String password;
}
```

```java
// RefreshRequest.java
package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "Refresh token 不能为空")
    private String refreshToken;
}
```

```java
// LoginResponse.java
package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String avatar;
}
```

- [ ] **Step 2: 编写 AuthService.java 和 AuthServiceImpl.java**

```java
// AuthService.java
package com.todo.service;

import com.todo.dto.*;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshRequest request);
    void logout(Long userId);
}
```

```java
// AuthServiceImpl.java
package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.common.BizException;
import com.todo.dto.*;
import com.todo.entity.RefreshToken;
import com.todo.entity.User;
import com.todo.mapper.RefreshTokenMapper;
import com.todo.mapper.UserMapper;
import com.todo.security.JwtTokenProvider;
import com.todo.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper,
                           JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())) > 0) {
            throw new BizException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getToken, request.getRefreshToken())
        );
        if (stored == null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(401, "Refresh token 无效或已过期");
        }
        refreshTokenMapper.deleteById(stored.getId());
        User user = userMapper.selectById(stored.getUserId());
        return buildLoginResponse(user);
    }

    @Override
    public void logout(Long userId) {
        refreshTokenMapper.delete(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getUserId, userId)
        );
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setToken(refreshTokenStr);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(604800)); // 7 days
        refreshTokenMapper.insert(rt);
        return new LoginResponse(accessToken, refreshTokenStr, user.getId(), user.getUsername(), user.getAvatar());
    }
}
```

- [ ] **Step 3: 编写 AuthController.java**

```java
package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.*;
import com.todo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<?> logout(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        authService.logout(userId);
        return Result.success();
    }
}
```

- [ ] **Step 4: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

## Phase 3: 后端业务 API

### Task 8: 待办 CRUD API

**Files:**
- Create: `server/src/main/java/com/todo/dto/TodoCreateRequest.java`
- Create: `server/src/main/java/com/todo/dto/TodoUpdateRequest.java`
- Create: `server/src/main/java/com/todo/service/TodoService.java`
- Create: `server/src/main/java/com/todo/service/impl/TodoServiceImpl.java`
- Create: `server/src/main/java/com/todo/controller/TodoController.java`

- [ ] **Step 1: 编写 DTO**

```java
// TodoCreateRequest.java
package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TodoCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;
    private String description;
    private Integer priority;  // 0=low, 1=medium, 2=high
    private LocalDate dueDate;
    private Long categoryId;
}
```

```java
// TodoUpdateRequest.java
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
```

- [ ] **Step 2: 编写 TodoService.java 和 TodoServiceImpl.java**

```java
// TodoService.java
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
```

```java
// TodoServiceImpl.java
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

import java.time.LocalDateTime;

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
```

- [ ] **Step 3: 编写 TodoController.java**

```java
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
                                @RequestBody TodoUpdateRequest request) {
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
```

- [ ] **Step 4: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 9: 分类和统计 API

**Files:**
- Create: `server/src/main/java/com/todo/dto/CategoryRequest.java`
- Create: `server/src/main/java/com/todo/dto/StatsResponse.java`
- Create: `server/src/main/java/com/todo/service/CategoryService.java`
- Create: `server/src/main/java/com/todo/service/impl/CategoryServiceImpl.java`
- Create: `server/src/main/java/com/todo/service/StatsService.java`
- Create: `server/src/main/java/com/todo/service/impl/StatsServiceImpl.java`
- Create: `server/src/main/java/com/todo/controller/CategoryController.java`
- Create: `server/src/main/java/com/todo/controller/StatsController.java`

- [ ] **Step 1: 编写 CategoryRequest 和 StatsResponse**

```java
// CategoryRequest.java
package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名不能为空")
    private String name;
    private String color;
}
```

```java
// StatsResponse.java
package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsResponse {
    private long completed;
    private long total;
    private double rate;  // 0.0 ~ 1.0
}
```

- [ ] **Step 2: 编写 CategoryService/Impl**

```java
// CategoryService.java
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
```

```java
// CategoryServiceImpl.java
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
```

- [ ] **Step 3: 编写 StatsService/Impl**

```java
// StatsService.java
package com.todo.service;

import com.todo.dto.StatsResponse;

public interface StatsService {
    StatsResponse getDailyStats(Long userId);
    StatsResponse getWeeklyStats(Long userId);
}
```

```java
// StatsServiceImpl.java
package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.dto.StatsResponse;
import com.todo.entity.Todo;
import com.todo.mapper.TodoMapper;
import com.todo.service.StatsService;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .ge(Todo::getCreatedAt, from.atStartOfDay())
                .le(Todo::getCreatedAt, to.atTime(LocalTime.MAX));
        long total = todoMapper.selectCount(wrapper);
        wrapper.eq(Todo::getCompleted, 1);
        long completed = todoMapper.selectCount(wrapper);
        double rate = total > 0 ? (double) completed / total : 0.0;
        return new StatsResponse(completed, total, rate);
    }
}
```

- [ ] **Step 4: 编写 CategoryController 和 StatsController**

```java
// CategoryController.java
package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.CategoryRequest;
import com.todo.entity.Category;
import com.todo.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.listByUser(userId));
    }

    @PostMapping
    public Result<Category> create(Authentication auth, @Valid @RequestBody CategoryRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Category> update(Authentication auth, @PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        categoryService.delete(userId, id);
        return Result.success();
    }
}
```

```java
// StatsController.java
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
```

- [ ] **Step 5: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

---

### Task 10: 用户个人信息和头像上传 API

**Files:**
- Create: `server/src/main/java/com/todo/dto/UserProfileRequest.java`
- Create: `server/src/main/java/com/todo/service/UserService.java`
- Create: `server/src/main/java/com/todo/service/impl/UserServiceImpl.java`
- Create: `server/src/main/java/com/todo/controller/UserController.java`
- Create: `server/src/main/java/com/todo/util/FileUploadUtil.java`

- [ ] **Step 1: 编写 UserProfileRequest.java**

```java
package com.todo.dto;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String username;
}
```

- [ ] **Step 2: 编写 FileUploadUtil.java**

```java
package com.todo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUploadUtil {

    private final Path uploadPath;

    public FileUploadUtil(@Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir) {
        this.uploadPath = Paths.get(avatarDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String uploadAvatar(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ?
                original.substring(original.lastIndexOf(".")) : ".png";
        String filename = UUID.randomUUID() + ext;
        try {
            Path target = uploadPath.resolve(filename);
            file.transferTo(target.toFile());
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
```

- [ ] **Step 3: 编写 UserService/Impl**

```java
// UserService.java
package com.todo.service;

import com.todo.entity.User;

public interface UserService {
    User getProfile(Long userId);
    User updateProfile(Long userId, String username);
    String updateAvatar(Long userId, String avatarUrl);
}
```

```java
// UserServiceImpl.java
package com.todo.service.impl;

import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User updateProfile(Long userId, String username) {
        User user = userMapper.selectById(userId);
        user.setUsername(username);
        userMapper.updateById(user);
        return user;
    }

    @Override
    public String updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return avatarUrl;
    }
}
```

- [ ] **Step 4: 编写 UserController.java**

```java
package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.UserProfileRequest;
import com.todo.entity.User;
import com.todo.service.UserService;
import com.todo.util.FileUploadUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;

    public UserController(UserService userService, FileUploadUtil fileUploadUtil) {
        this.userService = userService;
        this.fileUploadUtil = fileUploadUtil;
    }

    @GetMapping("/profile")
    public Result<User> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(Authentication auth, @RequestBody UserProfileRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.updateProfile(userId, request.getUsername()));
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) auth.getPrincipal();
        String url = fileUploadUtil.uploadAvatar(file);
        userService.updateAvatar(userId, url);
        return Result.success(url);
    }
}
```

- [ ] **Step 5: 启动验证整个后端**

```bash
cd server && mvn spring-boot:run
```

Expected: Spring Boot 启动成功，访问 `http://localhost:8080/api/auth/login` 得到响应。

先确保 MySQL 已启动且 `todo_db` 数据库存在。

---

## Phase 4: 前端基础搭建

### Task 11: 创建 Vue 项目

**Files:**
- Create: `client/package.json`
- Create: `client/vite.config.js`
- Create: `client/index.html`
- Create: `client/src/main.js`
- Create: `client/src/styles/variables.css`
- Create: `client/src/styles/theme.css`

- [ ] **Step 1: 编写 package.json**

```json
{
  "name": "todo-client",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0"
  }
}
```

- [ ] **Step 2: 编写 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 编写 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" data-theme="light">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>个人待办</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: 编写 main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/variables.css'
import './styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

- [ ] **Step 5: 编写 variables.css**

```css
:root {
  --font-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  --radius: 8px;
  --radius-lg: 12px;
  --shadow-popup: 0 20px 50px rgba(0, 0, 0, 0.2);
}

[data-theme="light"] {
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --bg-tertiary: #f0f2f5;
  --text-primary: #1a1a2e;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
  --brand-color: #6366f1;
  --brand-light: #eef2ff;
  --danger: #ef4444;
  --warning: #f59e0b;
  --success: #10b981;
}

[data-theme="dark"] {
  --bg-primary: #1a1b2e;
  --bg-secondary: #232440;
  --bg-tertiary: #2a2b4a;
  --text-primary: #e2e8f0;
  --text-secondary: #a0aec0;
  --border-color: #3a3b5a;
  --brand-color: #818cf8;
  --brand-light: #2a2b5a;
  --danger: #f87171;
  --warning: #fbbf24;
  --success: #34d399;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--font-sans);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: background 0.3s, color 0.3s;
}

#app {
  min-height: 100vh;
}
```

- [ ] **Step 6: 编写 theme.css**

```css
/* 全局过渡 */
.theme-transition,
.theme-transition * {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}
```

- [ ] **Step 7: 安装依赖并验证**

```bash
cd client && npm install && npm run dev
```

Expected: Vite dev server 启动在 localhost:5173

---

### Task 12: 路由和 Axios 封装

**Files:**
- Create: `client/src/router/index.js`
- Create: `client/src/api/index.js`
- Create: `client/src/api/auth.js`
- Create: `client/src/api/todos.js`
- Create: `client/src/api/categories.js`
- Create: `client/src/api/stats.js`
- Create: `client/src/api/user.js`

- [ ] **Step 1: 编写 router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
  { path: '/', name: 'Dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 2: 编写 api/index.js（axios 实例 + 拦截器）**

```javascript
import axios from 'axios'
import router from '../router'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }
      originalRequest._retry = true
      isRefreshing = true
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const res = await axios.post('/api/auth/refresh', { refreshToken })
        const { accessToken, refreshToken: newRefresh } = res.data.data
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', newRefresh)
        processQueue(null, accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        router.push('/login')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    return Promise.reject(error)
  }
)

export default api
```

- [ ] **Step 3: 编写各 API 模块**

```javascript
// auth.js
import api from './index.js'
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const refresh = (data) => api.post('/auth/refresh', data)
export const logout = () => api.post('/auth/logout')
```

```javascript
// todos.js
import api from './index.js'
export const getTodos = (params) => api.get('/todos', { params })
export const createTodo = (data) => api.post('/todos', data)
export const updateTodo = (id, data) => api.put(`/todos/${id}`, data)
export const deleteTodo = (id) => api.delete(`/todos/${id}`)
export const toggleTodo = (id) => api.patch(`/todos/${id}/toggle`)
```

```javascript
// categories.js
import api from './index.js'
export const getCategories = () => api.get('/categories')
export const createCategory = (data) => api.post('/categories', data)
export const updateCategory = (id, data) => api.put(`/categories/${id}`, data)
export const deleteCategory = (id) => api.delete(`/categories/${id}`)
```

```javascript
// stats.js
import api from './index.js'
export const getDailyStats = () => api.get('/stats/daily')
export const getWeeklyStats = () => api.get('/stats/weekly')
```

```javascript
// user.js
import api from './index.js'
export const getProfile = () => api.get('/user/profile')
export const updateProfile = (data) => api.put('/user/profile', data)
export const uploadAvatar = (formData) => api.post('/user/avatar', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

- [ ] **Step 4: 验证编译**

```bash
cd client && npm run dev
```

Expected: Vite 启动无报错。

---

## Phase 5: 前端认证和主题

### Task 13: Pinia Store（Auth + Theme + Weather）

**Files:**
- Create: `client/src/stores/auth.js`
- Create: `client/src/stores/theme.js`
- Create: `client/src/stores/weather.js`

- [ ] **Step 1: 编写 stores/auth.js**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '../api/auth'
import * as userApi from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  function init() {
    const token = localStorage.getItem('accessToken')
    if (token) {
      isLoggedIn.value = true
      loadProfile()
    }
  }

  async function loadProfile() {
    try {
      const res = await userApi.getProfile()
      user.value = res.data.data
    } catch (e) {
      // ignore if token expired
    }
  }

  async function loginAction(credentials) {
    const res = await authApi.login(credentials)
    const d = res.data.data
    localStorage.setItem('accessToken', d.accessToken)
    localStorage.setItem('refreshToken', d.refreshToken)
    isLoggedIn.value = true
    user.value = { id: d.userId, username: d.username, avatar: d.avatar }
    return d
  }

  async function registerAction(credentials) {
    const res = await authApi.register(credentials)
    const d = res.data.data
    localStorage.setItem('accessToken', d.accessToken)
    localStorage.setItem('refreshToken', d.refreshToken)
    isLoggedIn.value = true
    user.value = { id: d.userId, username: d.username, avatar: d.avatar }
    return d
  }

  async function logoutAction() {
    try { await authApi.logout() } catch (e) {}
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    isLoggedIn.value = false
    user.value = null
  }

  return { user, isLoggedIn, init, loginAction, registerAction, logoutAction, loadProfile }
})
```

- [ ] **Step 2: 编写 stores/theme.js**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'auto')

  function apply() {
    let resolved = theme.value
    if (resolved === 'auto') {
      resolved = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    document.documentElement.setAttribute('data-theme', resolved)
  }

  function toggle() {
    if (theme.value === 'auto') {
      theme.value = 'dark'
    } else if (theme.value === 'dark') {
      theme.value = 'light'
    } else {
      theme.value = 'auto'
    }
    localStorage.setItem('theme', theme.value)
    apply()
  }

  function init() {
    apply()
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', apply)
  }

  return { theme, toggle, init, apply }
})
```

- [ ] **Step 3: 编写 stores/weather.js**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

const API_KEY = 'YOUR_HEFENG_API_KEY'

export const useWeatherStore = defineStore('weather', () => {
  const city = ref(localStorage.getItem('weatherCity') || '杭州')
  const cityId = ref(localStorage.getItem('weatherCityId') || '101210101')
  const current = ref(null)
  const forecast = ref([])
  const loading = ref(false)

  async function fetchWeather() {
    loading.value = true
    try {
      // 和风天气 API: 当天天气 + 3天预报
      const nowRes = await fetch(
        `https://devapi.qweather.com/v7/weather/now?location=${cityId.value}&key=${API_KEY}`
      )
      const nowData = await nowRes.json()
      if (nowData.code === '200') {
        current.value = nowData.now
      }

      const forecastRes = await fetch(
        `https://devapi.qweather.com/v7/weather/3d?location=${cityId.value}&key=${API_KEY}`
      )
      const forecastData = await forecastRes.json()
      if (forecastData.code === '200') {
        forecast.value = forecastData.daily
      }
    } catch (e) {
      console.error('天气获取失败', e)
    } finally {
      loading.value = false
    }
  }

  function setCity(name, id) {
    city.value = name
    cityId.value = id
    localStorage.setItem('weatherCity', name)
    localStorage.setItem('weatherCityId', id)
    fetchWeather()
  }

  return { city, cityId, current, forecast, loading, fetchWeather, setCity }
})
```

- [ ] **Step 4: 更新 main.js 加入 store 初始化**

对 `client/src/main.js` 做如下修改：

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useThemeStore } from './stores/theme'
import './styles/variables.css'
import './styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// 初始化
const themeStore = useThemeStore()
themeStore.init()
const authStore = useAuthStore()
authStore.init()

app.mount('#app')
```

---

### Task 14: App.vue + AppHeader + ThemeToggle

**Files:**
- Create: `client/src/App.vue`
- Create: `client/src/components/AppHeader.vue`

- [ ] **Step 1: 编写 App.vue**

```vue
<template>
  <div :class="{ 'theme-transition': transitioning }">
    <AppHeader v-if="auth.isLoggedIn" />
    <router-view />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import AppHeader from './components/AppHeader.vue'

const auth = useAuthStore()
const router = useRouter()
const transitioning = ref(false)

watch(() => auth.isLoggedIn, (val) => {
  if (!val) router.push('/login')
})
</script>
```

- [ ] **Step 2: 编写 AppHeader.vue**

```vue
<template>
  <header class="app-header">
    <h1 class="logo" @click="$router.push('/')">📋 待办清单</h1>
    <div class="header-right">
      <button class="theme-btn" @click="theme.toggle()" :title="themeLabel">
        {{ themeIcon }}
      </button>
      <div class="user-info" v-if="auth.user">
        <img :src="auth.user.avatar || defaultAvatar" class="avatar" />
        <span class="username">{{ auth.user.username }}</span>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'

const auth = useAuthStore()
const theme = useThemeStore()
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="16" r="8" fill="%23cbd5e1"/><ellipse cx="20" cy="34" rx="12" ry="8" fill="%23cbd5e1"/></svg>'

const themeIcon = computed(() => {
  const t = theme.theme
  if (t === 'auto') return '🔄'
  return t === 'dark' ? '🌙' : '☀️'
})

const themeLabel = computed(() => {
  const t = theme.theme
  if (t === 'auto') return '自动模式'
  return t === 'dark' ? '暗色模式' : '亮色模式'
})

async function handleLogout() {
  await auth.logoutAction()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}
.logo { font-size: 18px; cursor: pointer; user-select: none; }
.header-right { display: flex; align-items: center; gap: 12px; }
.theme-btn { background: none; border: 1px solid var(--border-color); border-radius: var(--radius); padding: 6px 10px; cursor: pointer; font-size: 16px; }
.user-info { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 1px solid var(--border-color); }
.username { color: var(--text-secondary); }
.logout-btn { background: none; border: 1px solid var(--border-color); border-radius: var(--radius); padding: 4px 10px; cursor: pointer; font-size: 12px; color: var(--text-secondary); }
</style>
```

---

### Task 15: LoginView + RegisterView

**Files:**
- Create: `client/src/views/LoginView.vue`
- Create: `client/src/views/RegisterView.vue`

- [ ] **Step 1: 编写 LoginView.vue**

```vue
<template>
  <div class="auth-page">
    <form class="auth-card" @submit.prevent="handleLogin">
      <h2>登录</h2>
      <input v-model="form.username" placeholder="用户名" required />
      <input v-model="form.password" type="password" placeholder="密码" required />
      <p class="error" v-if="error">{{ error }}</p>
      <button type="submit" :disabled="loading">登录</button>
      <p class="link">还没有账号？<router-link to="/register">注册</router-link></p>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.loginAction(form)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg-secondary); }
.auth-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 32px; width: 360px; display: flex; flex-direction: column; gap: 14px; }
.auth-card h2 { text-align: center; font-size: 22px; }
.auth-card input { padding: 10px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.auth-card button { padding: 10px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); font-size: 15px; cursor: pointer; }
.auth-card button:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: var(--danger); font-size: 13px; }
.link { text-align: center; font-size: 13px; color: var(--text-secondary); }
.link a { color: var(--brand-color); }
</style>
```

- [ ] **Step 2: 编写 RegisterView.vue**

```vue
<template>
  <div class="auth-page">
    <form class="auth-card" @submit.prevent="handleRegister">
      <h2>注册</h2>
      <input v-model="form.username" placeholder="用户名（3-50位）" required minlength="3" />
      <input v-model="form.password" type="password" placeholder="密码（至少6位）" required minlength="6" />
      <p class="error" v-if="error">{{ error }}</p>
      <button type="submit" :disabled="loading">注册</button>
      <p class="link">已有账号？<router-link to="/login">登录</router-link></p>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await auth.registerAction(form)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg-secondary); }
.auth-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 32px; width: 360px; display: flex; flex-direction: column; gap: 14px; }
.auth-card h2 { text-align: center; font-size: 22px; }
.auth-card input { padding: 10px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.auth-card button { padding: 10px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); font-size: 15px; cursor: pointer; }
.auth-card button:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: var(--danger); font-size: 13px; }
.link { text-align: center; font-size: 13px; color: var(--text-secondary); }
.link a { color: var(--brand-color); }
</style>
```

- [ ] **Step 3: 验证**

```bash
cd client && npm run dev
```

Expected: 访问 localhost:5173/login 看到登录页面。

---

## Phase 6: 仪表盘组件

### Task 16: DashboardView 布局骨架

**Files:**
- Create: `client/src/views/DashboardView.vue`

- [ ] **Step 1: 编写 DashboardView.vue**

```vue
<template>
  <div class="dashboard">
    <CalendarPanel class="dash-left" />
    <div class="dash-right">
      <WeatherWidget />
      <StatsCard />
      <QuickAddBar @added="refresh" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CalendarPanel from '../components/calendar/CalendarPanel.vue'
import WeatherWidget from '../components/weather/WeatherWidget.vue'
import StatsCard from '../components/stats/StatsCard.vue'
import QuickAddBar from '../components/todo/QuickAddBar.vue'

const refreshKey = ref(0)
function refresh() { refreshKey.value++ }
</script>

<style scoped>
.dashboard {
  display: flex;
  gap: 14px;
  padding: 16px;
  height: calc(100vh - 56px);
  overflow: hidden;
}
.dash-left { flex: 1; min-width: 0; }
.dash-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}
</style>
```

- [ ] **Step 2: 验证热更新**

Expected: 访问 `/` 看到空白骨架（子组件尚未创建）。

---

### Task 17: 日历面板（CalendarPanel + MonthNav + CalendarGrid + DayCell + MagnifierPopup）

**Files:**
- Create: `client/src/components/calendar/CalendarPanel.vue`
- Create: `client/src/components/calendar/MonthNav.vue`
- Create: `client/src/components/calendar/CalendarGrid.vue`
- Create: `client/src/components/calendar/DayCell.vue`
- Create: `client/src/components/calendar/MagnifierPopup.vue`

- [ ] **Step 1: 编写 CalendarPanel.vue**

```vue
<template>
  <div class="calendar-panel">
    <MonthNav :current="current" @prev="prevMonth" @next="nextMonth" />
    <CalendarGrid :year="current.year" :month="current.month" :todos="todos" @dayClick="onDayClick" />
    <MagnifierPopup v-if="selectedDay" :day="selectedDay" :todos="selectedTodos" :position="popPos"
                    @close="selectedDay = null" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTodos } from '../../api/todos'
import MonthNav from './MonthNav.vue'
import CalendarGrid from './CalendarGrid.vue'
import MagnifierPopup from './MagnifierPopup.vue'

const current = ref({ year: new Date().getFullYear(), month: new Date().getMonth() + 1 })
const todos = ref([])
const selectedDay = ref(null)
const popPos = ref('top-right')

function prevMonth() {
  if (current.value.month === 1) { current.value.month = 12; current.value.year-- }
  else current.value.month--
}
function nextMonth() {
  if (current.value.month === 12) { current.value.month = 1; current.value.year++ }
  else current.value.month++
}

const selectedTodos = computed(() => {
  if (!selectedDay.value) return []
  return todos.value.filter(t => t.dueDate === selectedDay.value.dateStr)
})

function onDayClick(day, rect) {
  selectedDay.value = day
  const col = rect.left / rect.width
  const row = rect.top / rect.height
  if (col > 0.67) popPos.value = 'top-left'
  else if (row > 0.75) popPos.value = 'top-right'
  else popPos.value = 'top-right'
}

async function loadTodos() {
  try {
    const y = current.value.year
    const m = String(current.value.month).padStart(2, '0')
    const res = await getTodos({
      sort: 'due_date',
      page: 1,
      size: 200
    })
    todos.value = res.data.data.records || []
  } catch (e) {
    console.error('加载待办失败', e)
  }
}

onMounted(loadTodos)
</script>

<style scoped>
.calendar-panel {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
```

- [ ] **Step 2: 编写 MonthNav.vue**

```vue
<template>
  <div class="month-nav">
    <button @click="$emit('prev')">◀</button>
    <span class="month-title">{{ current.year }}年 {{ current.month }}月</span>
    <button @click="$emit('next')">▶</button>
  </div>
</template>

<script setup>
defineProps({ current: Object })
defineEmits(['prev', 'next'])
</script>

<style scoped>
.month-nav { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.month-nav button { background: none; border: none; cursor: pointer; font-size: 14px; color: var(--text-secondary); padding: 4px 8px; }
.month-title { font-weight: 700; font-size: 16px; }
</style>
```

- [ ] **Step 3: 编写 CalendarGrid.vue**

```vue
<template>
  <div class="cal-grid">
    <div class="cal-header" v-for="d in '日一二三四五六'">{{ d }}</div>
    <DayCell v-for="(day, i) in days" :key="i" :day="day"
             :todos="todosForDay(day)" @click="(rect) => $emit('dayClick', day, rect)" />
  </div>
  <div class="cal-legend">
    <span><span class="dot high"></span> 高</span>
    <span><span class="dot mid"></span> 中</span>
    <span><span class="dot low"></span> 低</span>
    <span class="hint">💡 单击格子查看详情</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DayCell from './DayCell.vue'

const props = defineProps({ year: Number, month: Number, todos: Array })
defineEmits(['dayClick'])

// 农历简易映射（实际项目应使用 lunar-calendar 库）
const LUNAR_MONTH = ['正','二','三','四','五','六','七','八','九','十','冬','腊']
const LUNAR_DAY = ['一','二','三','四','五','六','七','八','九','十','十一','十二','十三','十四','十五','十六','十七','十八','十九','二十','廿一','廿二','廿三','廿四','廿五','廿六','廿七','廿八','廿九','三十']
const CHINESE_HOLIDAYS = {
  '1-1': '元旦', '5-1': '劳动节', '10-1': '国庆节',
}

function getLunarOrHoliday(d) {
  const key = `${props.month}-${d.getDate()}`
  if (CHINESE_HOLIDAYS[key]) return CHINESE_HOLIDAYS[key]
  // 简化农历计算
  const lunarDay = ((d.getDate() - 1) % 30)
  const lunarMonth = (props.month - 1) % 12
  return LUNAR_MONTH[lunarMonth] + '月' + LUNAR_DAY[lunarDay]
}

function todosForDay(day) {
  if (!day) return []
  return props.todos.filter(t => t.dueDate === day.dateStr)
}

const days = computed(() => {
  const first = new Date(props.year, props.month - 1, 1)
  const startDay = first.getDay()
  const daysInMonth = new Date(props.year, props.month, 0).getDate()
  const result = []
  for (let i = 0; i < startDay; i++) result.push(null)
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(props.year, props.month - 1, d)
    const dateStr = `${props.year}-${String(props.month).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    const isToday = dateStr === new Date().toISOString().slice(0, 10)
    result.push({ dateStr, day: d, weekDay: date.getDay(), lunar: getLunarOrHoliday(date), isToday })
  }
  return result
})
</script>

<style scoped>
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; flex: 1; font-size: 10px; text-align: center; }
.cal-header { opacity: 0.4; font-weight: 600; padding: 2px; }
.cal-legend { display: flex; gap: 12px; margin-top: 8px; font-size: 10px; opacity: 0.5; }
.dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; }
.dot.high { background: var(--danger); }
.dot.mid { background: var(--warning); }
.dot.low { background: var(--success); }
.hint { margin-left: auto; }
</style>
```

- [ ] **Step 4: 编写 DayCell.vue**

```vue
<template>
  <div class="day-cell" :class="{ today: day?.isToday, empty: !day }" @click="handleClick" ref="cellRef">
    <template v-if="day">
      <div class="day-header">
        <span class="solar" :class="{ todayText: day.isToday }">{{ day.day }}</span>
        <span class="lunar">{{ day.lunar }}</span>
      </div>
      <div class="todo-strip" v-for="t in visibleTodos" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span :class="{ completed: t.completed === 1 }">{{ t.title }}</span>
      </div>
      <div class="more" v-if="todos.length > maxVisible">+{{ todos.length - maxVisible }} 更多</div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({ day: Object, todos: Array })
const emit = defineEmits(['click'])
const cellRef = ref(null)
const maxVisible = 2

const visibleTodos = computed(() => props.todos?.slice(0, maxVisible) || [])

function priorityClass(p) {
  if (p === 2) return 'high'
  if (p === 1) return 'mid'
  return 'low'
}

function handleClick() {
  if (!props.day) return
  emit('click', cellRef.value?.getBoundingClientRect())
}
</script>

<style scoped>
.day-cell {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 3px;
  font-size: 9px;
  text-align: left;
  background: var(--bg-primary);
  cursor: pointer;
  position: relative;
  min-height: 48px;
  overflow: hidden;
}
.day-cell.empty { background: transparent; border: none; cursor: default; }
.day-cell.today { border: 2px solid var(--brand-color); background: var(--brand-light); }
.day-header { display: flex; justify-content: space-between; margin-bottom: 1px; }
.solar { font-weight: 600; font-size: 11px; }
.todayText { color: var(--brand-color); }
.lunar { font-size: 7px; opacity: 0.45; }
.todo-strip { font-size: 7px; margin-top: 1px; display: flex; align-items: center; gap: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.todo-strip .dot { width: 4px; height: 4px; border-radius: 50%; flex-shrink: 0; }
.todo-strip .dot.high { background: var(--danger); }
.todo-strip .dot.mid { background: var(--warning); }
.todo-strip .dot.low { background: var(--success); }
.todo-strip .completed { text-decoration: line-through; opacity: 0.6; }
.more { font-size: 7px; color: var(--brand-color); font-weight: 600; margin-top: 1px; }
</style>
```

- [ ] **Step 5: 编写 MagnifierPopup.vue**

```vue
<template>
  <div class="magnifier-backdrop" @click="$emit('close')">
    <div class="magnifier" :class="position" @click.stop>
      <div class="mag-date">{{ day.dateStr }} {{ day.lunar }}</div>
      <div class="mag-todos">
        <div class="mag-item" v-for="t in todos" :key="t.id"
             :class="{ completed: t.completed === 1 }">
          <span class="dot" :class="priorityClass(t.priority)"></span>
          <span>{{ t.title }}</span>
        </div>
        <div class="mag-empty" v-if="!todos.length">暂无待办</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ day: Object, todos: Array, position: { type: String, default: 'top-right' } })
defineEmits(['close'])

function priorityClass(p) {
  if (p === 2) return 'high'
  if (p === 1) return 'mid'
  return 'low'
}
</script>

<style scoped>
.magnifier-backdrop { position: fixed; inset: 0; z-index: 1000; background: transparent; }
.magnifier { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); box-shadow: var(--shadow-popup); padding: 16px 18px; min-width: 240px; max-width: 320px; }
.mag-date { font-weight: 700; font-size: 15px; color: var(--brand-color); margin-bottom: 8px; }
.mag-todos { display: flex; flex-direction: column; gap: 5px; }
.mag-item { display: flex; align-items: center; gap: 6px; font-size: 12px; padding: 5px 6px; border-radius: 4px; background: var(--bg-secondary); }
.mag-item.completed span { text-decoration: line-through; opacity: 0.5; }
.mag-item .dot { width: 6px; height: 6px; border-radius: 50%; }
.mag-item .dot.high { background: var(--danger); }
.mag-item .dot.mid { background: var(--warning); }
.mag-item .dot.low { background: var(--success); }
.mag-empty { font-size: 12px; opacity: 0.4; text-align: center; padding: 12px 0; }
</style>
```

---

### Task 18: 天气组件（WeatherWidget + CitySearchDialog）

**Files:**
- Create: `client/src/components/weather/WeatherWidget.vue`
- Create: `client/src/components/weather/CitySearchDialog.vue`

- [ ] **Step 1: 编写 WeatherWidget.vue**

```vue
<template>
  <div class="weather-widget">
    <div class="weather-row">
      <button class="pin-btn" @click="showCitySearch = true" title="切换城市">
        <span class="pin-icon">📍</span>
      </button>
      <div class="weather-icon">{{ weatherIcon }}</div>
      <div class="weather-desc">{{ currentText }}</div>
      <div class="weather-temp">{{ weather.current?.temp }}°</div>
      <div class="weather-humidity">湿度 {{ weather.current?.humidity }}%</div>
      <span class="weather-city">{{ weather.city }}</span>
      <div class="weather-divider"></div>
      <div class="forecast-row">
        <div class="fc-day" v-for="(d, i) in weather.forecast" :key="i">
          <div class="fc-label">{{ i === 0 ? '明天' : dayOfWeek(i + 1) }}</div>
          <div class="fc-icon">{{ forecastIcon(d) }}</div>
          <div class="fc-high">{{ d.tempMax }}°</div>
          <div class="fc-low">{{ d.tempMin }}°</div>
        </div>
      </div>
    </div>
    <CitySearchDialog v-if="showCitySearch" @select="onCitySelect" @close="showCitySearch = false" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useWeatherStore } from '../../stores/weather'
import CitySearchDialog from './CitySearchDialog.vue'

const weather = useWeatherStore()
const showCitySearch = ref(false)

const currentText = computed(() => weather.current?.text || '--')
const weatherIcon = computed(() => {
  const t = weather.current?.text || ''
  if (t.includes('晴')) return '☀️'; if (t.includes('云')) return '⛅'
  if (t.includes('雨')) return '🌧'; if (t.includes('雪')) return '❄️'
  return '🌈'
})

function forecastIcon(d) {
  const t = d.textDay || ''
  if (t.includes('晴')) return '☀️'; if (t.includes('云')) return '⛅'
  if (t.includes('雨')) return '🌧'; return '🌈'
}

function dayOfWeek(offset) {
  const days = ['周日','周一','周二','周三','周四','周五','周六']
  return days[(new Date().getDay() + offset) % 7]
}

function onCitySelect(name, id) {
  weather.setCity(name, id)
  showCitySearch.value = false
}

onMounted(() => { weather.fetchWeather() })
</script>

<style scoped>
.weather-widget {
  background: linear-gradient(135deg, var(--brand-light), var(--bg-secondary));
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  position: relative;
}
.weather-row { display: flex; align-items: center; gap: 12px; overflow: hidden; }
.pin-btn { background: none; border: none; cursor: pointer; font-size: 18px; padding: 0; flex-shrink: 0; }
.pin-icon { filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2)); }
.weather-icon { font-size: 34px; flex-shrink: 0; }
.weather-desc { font-size: 10px; opacity: 0.5; flex-shrink: 0; }
.weather-temp { font-size: 26px; font-weight: 300; flex-shrink: 0; }
.weather-humidity { font-size: 11px; opacity: 0.45; flex-shrink: 0; }
.weather-city { font-weight: 700; font-size: 15px; flex-shrink: 0; }
.weather-divider { width: 1px; height: 36px; background: rgba(128,128,128,0.15); flex-shrink: 0; }
.forecast-row { display: flex; flex: 1; justify-content: space-around; }
.fc-day { text-align: center; }
.fc-label { font-size: 9px; opacity: 0.45; }
.fc-icon { font-size: 18px; }
.fc-high { font-size: 10px; font-weight: 600; }
.fc-low { font-size: 8px; opacity: 0.35; }
</style>
```

- [ ] **Step 2: 编写 CitySearchDialog.vue**

```vue
<template>
  <div class="search-overlay" @click.self="$emit('close')">
    <div class="search-dialog">
      <input v-model="keyword" placeholder="搜索城市..." @input="search" />
      <div class="search-results">
        <div class="city-item" v-for="c in results" :key="c.id" @click="$emit('select', c.name, c.id)">
          {{ c.name }}
        </div>
        <div class="city-item" v-if="!results.length && keyword">未找到相关城市</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['select', 'close'])
const keyword = ref('')
const results = ref([])

// 和风天气城市搜索
async function search() {
  if (!keyword.value) { results.value = []; return }
  try {
    const res = await fetch(
      `https://geoapi.qweather.com/v2/city/lookup?location=${keyword.value}&key=YOUR_API_KEY`
    )
    const data = await res.json()
    results.value = (data.location || []).map(c => ({ name: c.name, id: c.id }))
  } catch (e) { results.value = [] }
}
</script>

<style scoped>
.search-overlay { position: fixed; inset: 0; z-index: 1001; background: rgba(0,0,0,0.3); display: flex; align-items: flex-start; justify-content: center; padding-top: 15%; }
.search-dialog { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 16px; width: 320px; max-height: 300px; overflow-y: auto; }
.search-dialog input { width: 100%; padding: 8px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.search-results { margin-top: 8px; }
.city-item { padding: 8px; cursor: pointer; border-radius: var(--radius); font-size: 14px; }
.city-item:hover { background: var(--bg-tertiary); }
</style>
```

---

### Task 19: 统计组件（StatsCard + RingChart + TaskDetailList）

**Files:**
- Create: `client/src/components/stats/StatsCard.vue`
- Create: `client/src/components/stats/RingChart.vue`
- Create: `client/src/components/stats/TaskDetailList.vue`

- [ ] **Step 1: 编写 RingChart.vue**

```vue
<template>
  <div class="ring-chart-wrapper" :class="{ active: selected }" @click="$emit('click')">
    <svg :width="size" :height="size" :viewBox="`0 0 ${vp} ${vp}`">
      <circle :cx="center" :cy="center" :r="radius" fill="none" :stroke="trackColor" stroke-width="8" />
      <circle :cx="center" :cy="center" :r="radius" fill="none" :stroke="progressColor"
              stroke-width="8" :stroke-dasharray="circumference" :stroke-dashoffset="dashoffset"
              stroke-linecap="round" transform="rotate(-90, center, center)" />
    </svg>
    <div class="ring-center">
      <div class="ring-num" :style="{ color: progressColor }">{{ completed }}/{{ total }}</div>
      <div class="ring-label">{{ label }}</div>
    </div>
    <div class="ring-footer">
      <div class="ring-title">{{ title }}</div>
      <div class="ring-sub" :style="{ color: subColor }">{{ subtitle }}</div>
    </div>
    <div class="ring-check" v-if="selected">✓</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  completed: Number, total: Number, label: String, title: String, subtitle: String,
  progressColor: { type: String, default: '#6366f1' }, subColor: String, selected: Boolean,
  size: { type: Number, default: 100 }, trackColor: { type: String, default: '#eee' }
})
defineEmits(['click'])

const vp = computed(() => props.size + 10)
const center = computed(() => vp.value / 2)
const radius = computed(() => props.size / 2 - 8)
const circumference = computed(() => 2 * Math.PI * radius.value)
const dashoffset = computed(() => {
  const rate = props.total > 0 ? props.completed / props.total : 0
  return circumference.value * (1 - rate)
})
</script>

<style scoped>
.ring-chart-wrapper {
  position: relative; text-align: center; cursor: pointer;
  padding: 10px 16px; border-radius: 12px; border: 2px solid var(--border-color);
  background: var(--bg-primary); display: flex; flex-direction: column; align-items: center; gap: 4px;
}
.ring-chart-wrapper.active { border-color: var(--brand-color); background: var(--brand-light); }
.ring-center { position: absolute; top: 44%; left: 50%; transform: translate(-50%, -50%); text-align: center; }
.ring-num { font-size: 18px; font-weight: 700; }
.ring-label { font-size: 9px; opacity: 0.7; }
.ring-footer { text-align: center; font-size: 11px; margin-top: auto; }
.ring-title { font-weight: 600; }
.ring-sub { font-size: 13px; font-weight: 700; }
.ring-check { position: absolute; top: -6px; right: -6px; width: 18px; height: 18px; background: var(--brand-color); border-radius: 50%; color: #fff; font-size: 10px; display: flex; align-items: center; justify-content: center; }
</style>
```

- [ ] **Step 2: 编写 TaskDetailList.vue**

```vue
<template>
  <div class="detail-list">
    <div class="detail-title" :style="{ color: titleColor }">{{ title }}</div>
    <div class="detail-section">
      <div class="section-label">✅ 已完成 ({{ completedList.length }})</div>
      <div class="detail-item done" v-for="t in completedList" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span>{{ t.title }}</span>
      </div>
    </div>
    <div class="detail-section">
      <div class="section-label">⏳ 待完成 ({{ pendingList.length }})</div>
      <div class="detail-item pending" v-for="t in pendingList" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span>{{ t.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ todos: Array, title: String, titleColor: { type: String, default: '#6366f1' } })
const completedList = computed(() => props.todos.filter(t => t.completed === 1))
const pendingList = computed(() => props.todos.filter(t => t.completed !== 1))

function priorityClass(p) {
  if (p === 2) return 'high'
  if (p === 1) return 'mid'
  return 'low'
}
</script>

<style scoped>
.detail-list { overflow-y: auto; font-size: 10px; }
.detail-title { font-weight: 600; font-size: 11px; margin-bottom: 8px; }
.detail-section { margin-bottom: 6px; }
.section-label { font-size: 9px; opacity: 0.4; margin-bottom: 3px; }
.detail-item { display: flex; align-items: center; gap: 4px; padding: 3px 6px; border-radius: 3px; margin-bottom: 3px; }
.detail-item.done { background: rgba(16,185,129,0.08); text-decoration: line-through; opacity: 0.6; }
.detail-item.pending { background: var(--bg-secondary); }
.detail-item .dot { width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0; }
.detail-item .dot.high { background: var(--danger); }
.detail-item .dot.mid { background: var(--warning); }
.detail-item .dot.low { background: var(--success); }
</style>
```

- [ ] **Step 3: 编写 StatsCard.vue**

```vue
<template>
  <div class="stats-card">
    <div class="stats-body">
      <div class="stats-left">
        <RingChart :completed="todayCompleted" :total="todayTotal" label="今日完成"
                   title="今日进度" :subtitle="`已完成 ${todayRate}%`" progressColor="#6366f1" subColor="#6366f1"
                   :selected="activeTab === 'today'" @click="switchTo('today')" />
        <RingChart :completed="weekCompleted" :total="weekTotal" label="本周完成"
                   title="本周总览" :subtitle="`剩余任务 ${weekRemaining} 项`" progressColor="#10b981" subColor="#ef4444"
                   :selected="activeTab === 'week'" @click="switchTo('week')" />
      </div>
      <TaskDetailList class="stats-right" :todos="activeTodos"
                      :title="activeTab === 'today' ? '📋 今日' : '📅 本周'"
                      :titleColor="activeTab === 'today' ? '#6366f1' : '#10b981'" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTodos } from '../../api/todos'
import RingChart from './RingChart.vue'
import TaskDetailList from './TaskDetailList.vue'

const activeTab = ref('today')
const allTodos = ref([])

const todayStr = new Date().toISOString().slice(0, 10)
const todayTodos = computed(() => allTodos.value.filter(t => t.dueDate === todayStr))
const todayCompleted = computed(() => todayTodos.value.filter(t => t.completed === 1).length)
const todayTotal = computed(() => todayTodos.value.length)
const todayRate = computed(() => todayTotal.value > 0 ? Math.round(todayCompleted.value / todayTotal.value * 100) : 0)

const weekTodos = computed(() => {
  const now = new Date()
  const day = now.getDay()
  const monday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - (day === 0 ? 6 : day - 1)).toISOString().slice(0, 10)
  return allTodos.value.filter(t => t.dueDate && t.dueDate >= monday && t.dueDate <= todayStr)
})
const weekCompleted = computed(() => weekTodos.value.filter(t => t.completed === 1).length)
const weekTotal = computed(() => weekTodos.value.length)
const weekRemaining = computed(() => weekTotal.value - weekCompleted.value)

const activeTodos = computed(() => activeTab.value === 'today' ? todayTodos.value : weekTodos.value)

function switchTo(tab) { activeTab.value = tab }

onMounted(async () => {
  try {
    const res = await getTodos({ sort: 'due_date', page: 1, size: 200 })
    allTodos.value = res.data.data.records || []
  } catch (e) { console.error('加载统计数据失败', e) }
})
</script>

<style scoped>
.stats-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 14px;
  flex: 1;
  overflow: hidden;
  display: flex;
}
.stats-body { display: flex; gap: 14px; width: 100%; }
.stats-left { flex: 1.35; display: flex; align-items: center; justify-content: space-around; gap: 12px; }
.stats-right { flex: 1; background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 10px; overflow-y: auto; }
</style>
```

---

### Task 20: 快速添加组件（QuickAddBar + AddTodoDialog）

**Files:**
- Create: `client/src/components/todo/QuickAddBar.vue`
- Create: `client/src/components/todo/AddTodoDialog.vue`

- [ ] **Step 1: 编写 QuickAddBar.vue**

```vue
<template>
  <div class="quick-add">
    <div class="add-row">
      <input v-model="title" placeholder="输入新的待办事项..." @keyup.enter="openDialog" />
      <button class="add-btn" @click="openDialog">添加</button>
    </div>
    <AddTodoDialog v-if="showDialog" :title="title" @confirm="handleConfirm" @close="showDialog = false" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createTodo } from '../../api/todos'
import AddTodoDialog from './AddTodoDialog.vue'

const emit = defineEmits(['added'])
const title = ref('')
const showDialog = ref(false)

function openDialog() {
  if (!title.value.trim()) return
  showDialog.value = true
}

async function handleConfirm(data) {
  try {
    await createTodo({ title: title.value, ...data })
    title.value = ''
    showDialog.value = false
    emit('added')
  } catch (e) {
    console.error('创建待办失败', e)
  }
}
</script>

<style scoped>
.quick-add { background: var(--bg-secondary); border: 1px solid var(--brand-color); border-radius: var(--radius-lg); padding: 12px; }
.add-row { display: flex; gap: 8px; align-items: center; }
.add-row input { flex: 1; padding: 10px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; outline: none; }
.add-row input:focus { border-color: var(--brand-color); }
.add-btn { padding: 10px 18px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); font-size: 14px; cursor: pointer; }
</style>
```

- [ ] **Step 2: 编写 AddTodoDialog.vue**

```vue
<template>
  <div class="dialog-overlay" @click.self="$emit('close')">
    <div class="dialog-card">
      <h4>设置待办详情</h4>
      <label>截止日期</label>
      <input type="date" v-model="form.dueDate" />
      <label>分类</label>
      <select v-model="form.categoryId">
        <option :value="null">无分类</option>
        <option v-for="c in categories" :value="c.id" :key="c.id">{{ c.name }}</option>
      </select>
      <label>优先级</label>
      <div class="priority-group">
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="0" /> 低</label>
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="1" checked /> 中</label>
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="2" /> 高</label>
      </div>
      <div class="dialog-actions">
        <button class="cancel" @click="$emit('close')">取消</button>
        <button class="confirm" @click="$emit('confirm', form)">确认添加</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { getCategories } from '../../api/categories'

defineEmits(['confirm', 'close'])
const form = reactive({ dueDate: null, categoryId: null, priority: 1 })
const categories = ref([])

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res.data.data || []
  } catch (e) {}
})
</script>

<style scoped>
.dialog-overlay { position: fixed; inset: 0; z-index: 1000; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; }
.dialog-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 24px; width: 380px; display: flex; flex-direction: column; gap: 10px; }
.dialog-card h4 { font-size: 16px; }
.dialog-card label { font-size: 13px; opacity: 0.6; }
.dialog-card input, .dialog-card select { padding: 8px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.priority-group { display: flex; gap: 16px; }
.pri-opt { display: flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }
.dialog-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
.cancel { padding: 8px 16px; background: var(--bg-tertiary); border: 1px solid var(--border-color); border-radius: var(--radius); cursor: pointer; font-size: 14px; color: var(--text-secondary); }
.confirm { padding: 8px 16px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); cursor: pointer; font-size: 14px; }
</style>
```

---

## Phase 7: 收尾

### Task 21: Nginx 配置

**Files:**
- Create: `nginx.conf`

- [ ] **Step 1: 编写 nginx.conf**

```nginx
server {
    listen 80;
    server_name localhost;

    root /path/to/todo2/client/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /uploads/ {
        proxy_pass http://localhost:8080;
    }
}
```

- [ ] **Step 2: 生产和风天气 API key 配置说明**

在 `client/src/stores/weather.js` 和 `client/src/components/weather/CitySearchDialog.vue` 中将 `YOUR_API_KEY` / `YOUR_HEFENG_API_KEY` 替换为实际的和风天气免费 API Key（注册地址: https://dev.qweather.com/）。

---

### Task 22: 最终验证

- [ ] **Step 1: 启动后端**

```bash
cd server && mvn spring-boot:run
```

- [ ] **Step 2: 启动前端开发模式**

```bash
cd client && npm run dev
```

- [ ] **Step 3: 端到端测试**

在浏览器访问 `http://localhost:5173`：
1. 注册新用户 → 自动跳转仪表盘
2. 添加一个待办事项（选择截止日期、分类、优先级）
3. 验证日历格子内显示该待办
4. 单击日期格子 → 放大镜浮层弹出
5. 验证任务完成率环形图更新
6. 切换暗色/亮色主题
7. 搜索城市切换天气
8. 退出登录 → 重新登录

- [ ] **Step 4: 生产构建**

```bash
cd client && npm run build
```

构建产物在 `client/dist/`，按 nginx.conf 配置部署。

---

## 实施建议

推荐使用 `superpowers:subagent-driven-development` 按上述 Task 顺序逐任务执行，每个 Task 完成后 Review 再进入下一个。后端和前端的基础阶段（Phase 1-2 和 Phase 4-5）之间有依赖关系，需按顺序执行；Phase 6 的各组件相对独立，可适度并行。
