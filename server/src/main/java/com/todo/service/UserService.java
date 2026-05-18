package com.todo.service;

import com.todo.entity.User;

public interface UserService {
    User getProfile(Long userId);
    User updateProfile(Long userId, String username);
    String updateAvatar(Long userId, String avatarUrl);
}
