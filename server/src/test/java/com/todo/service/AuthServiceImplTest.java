package com.todo.service;

import com.todo.dto.*;
import com.todo.common.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AuthServiceImplTest {

    @Autowired
    private AuthService authService;

    private LoginResponse register(String name) {
        var req = new RegisterRequest();
        req.setUsername(name);
        req.setPassword("pw" + name);
        return authService.register(req);
    }

    @Test
    void registerReturnsTokens() {
        var r = register("alice");
        assertThat(r.getAccessToken()).isNotBlank();
        assertThat(r.getRefreshToken()).isNotBlank();
        assertThat(r.getUsername()).isEqualTo("alice");
    }

    @Test
    void duplicateRegistrationThrows() {
        register("bob");
        var req = new RegisterRequest();
        req.setUsername("bob");
        req.setPassword("anypass1");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void correctLoginReturnsTokens() throws Exception {
        register("carol");
        Thread.sleep(1000); // ensure unique JWT issuedAt (second-precision)
        var req = new LoginRequest();
        req.setUsername("carol");
        req.setPassword("pwcarol");
        var r = authService.login(req);
        assertThat(r.getAccessToken()).isNotBlank();
    }

    @Test
    void wrongPasswordThrows() {
        register("dave");
        var req = new LoginRequest();
        req.setUsername("dave");
        req.setPassword("bad");
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BizException.class);
    }

    @Test
    void refreshTokenWorks() {
        var reg = register("eve");
        var req = new RefreshRequest();
        req.setRefreshToken(reg.getRefreshToken());
        var r = authService.refresh(req);
        assertThat(r.getAccessToken()).isNotBlank();
    }

    @Test
    void reusedRefreshTokenFails() throws Exception {
        var reg = register("frank");
        Thread.sleep(1000); // ensure unique JWT issuedAt (second-precision)
        var req = new RefreshRequest();
        req.setRefreshToken(reg.getRefreshToken());
        authService.refresh(req);
        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(BizException.class);
    }
}
