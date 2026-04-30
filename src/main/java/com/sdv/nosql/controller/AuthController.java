package com.sdv.nosql.controller;

import com.sdv.nosql.dto.LoginRequest;
import com.sdv.nosql.dto.LoginResponse;
import com.sdv.nosql.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.getUserId());
    }
}
