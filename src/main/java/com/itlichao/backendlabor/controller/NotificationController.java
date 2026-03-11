package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.entity.UserNotification;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<List<UserNotification>> myList(
            @RequestParam(required = false, defaultValue = "all") String status,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(notificationService.myList(userId, status));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            notificationService.markRead(userId, id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/read-all")
    public Result<Map<String, Object>> readAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            int updated = notificationService.markAllRead(userId);
            return Result.ok(Map.of("updated", updated));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

