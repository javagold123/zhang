package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.AuthService;
import com.itlichao.backendlabor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        Map<String, Object> user = authService.getMe(userId);
        return Result.ok(user);
    }

    @PutMapping("/me")
    public Result<Map<String, Object>> updateMe(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            Map<String, Object> user = userService.updateMe(userId, body);
            return Result.ok(user);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String search, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        // 简单权限：仅管理员可查用户列表，此处通过前端约定，后端可加角色校验
        List<Map<String, Object>> list = userService.list(search);
        return Result.ok(list);
    }

    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            userService.updateRole(id, body.get("role"));
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-disabled")
    public Result<Void> toggleDisabled(@PathVariable Long id, HttpServletRequest request) {
        try {
            userService.toggleDisabled(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            userService.delete(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
