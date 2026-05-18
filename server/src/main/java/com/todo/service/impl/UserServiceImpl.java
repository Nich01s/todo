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
