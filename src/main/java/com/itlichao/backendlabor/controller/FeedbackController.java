package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean all,
            HttpServletRequest request) {
        Long userId = (Boolean.TRUE.equals(all) ? null : (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID));
        List<Map<String, Object>> list = feedbackService.list(status, type, userId);
        return Result.ok(list);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var f = feedbackService.create(userId, body);
            return Result.ok(Map.of("id", f.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
