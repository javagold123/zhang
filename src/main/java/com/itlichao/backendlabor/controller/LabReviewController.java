package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.LabReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs/{labId}/reviews")
public class LabReviewController {

    private final LabReviewService service;

    public LabReviewController(LabReviewService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(@PathVariable Long labId) {
        return Result.ok(service.listByLab(labId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@PathVariable Long labId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        try {
            var r = service.create(labId, userId, rating, content);
            return Result.ok(Map.of("id", r.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long labId, @PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            service.withdraw(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

