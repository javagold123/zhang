package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail(400, "用户名和密码不能为空");
        }
        try {
            Map<String, Object> data = authService.login(username, password, request);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return Result.fail(400, "用户名和密码不能为空");
        }
        try {
            Map<String, Object> user = authService.register(
                    username.trim(),
                    password,
                    (String) body.get("name"),
                    (String) body.get("role"),
                    (String) body.get("studentNo"),
                    (String) body.get("phone"),
                    (String) body.get("email"));
            return Result.ok(user);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
