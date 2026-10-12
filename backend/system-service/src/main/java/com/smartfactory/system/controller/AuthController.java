package com.smartfactory.system.controller;

import com.smartfactory.common.constant.SecurityConstants;
import com.smartfactory.common.result.Result;
import com.smartfactory.system.dto.LoginRequest;
import com.smartfactory.system.dto.LoginResponse;
import com.smartfactory.system.entity.SysUser;
import com.smartfactory.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<SysUser> me(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId) {
        return Result.ok(authService.getById(userId));
    }
}
