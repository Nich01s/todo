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
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(604800));
        refreshTokenMapper.insert(rt);
        return new LoginResponse(accessToken, refreshTokenStr, user.getId(), user.getUsername(), user.getAvatar());
    }
}
