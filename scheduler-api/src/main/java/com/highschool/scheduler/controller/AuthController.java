package com.highschool.scheduler.controller;

import com.highschool.scheduler.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public Map<String, Object> login(@RequestParam String email) {
        log.info("Login attempt with email: {}", email);
        Map<String, Object> user = authService.loginUser(email);
        log.info("Login successful for {}", user.get("email"));
        return user;
    }
}
