package com.study.shortlink.controller;


import com.study.shortlink.pojo.dto.LoginRequestDto;
import com.study.shortlink.pojo.dto.RegisterRequestDto;
import com.study.shortlink.service.UserService;
import com.study.shortlink.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto)
    {

        return ResponseEntity.ok(userService.login(loginRequestDto));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto)
    {
        userService.register(registerRequestDto);
        return ResponseEntity.ok("Register successfully");
    }
}
