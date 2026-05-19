package com.todo.controller;

import com.todo.common.Result;
import com.todo.dto.UserProfileRequest;
import com.todo.entity.User;
import com.todo.service.UserService;
import com.todo.util.FileUploadUtil;
import jakarta.validation.Valid;
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
    public Result<User> updateProfile(Authentication auth, @Valid @RequestBody UserProfileRequest request) {
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
