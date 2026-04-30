package com.sdv.nosql.controller;

import com.sdv.nosql.dto.LoginRequest;
import com.sdv.nosql.dto.LoginResponse;
import com.sdv.nosql.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getUserId());
    }
}
