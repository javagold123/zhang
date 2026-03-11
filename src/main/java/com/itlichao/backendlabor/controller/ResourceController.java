package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    private boolean isTeacherOrAdmin(HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        return role instanceof String r && ("teacher".equals(r) || "admin".equals(r));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false) String search
    ) {
        return Result.ok(resourceService.list(category, search));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        Map<String, Object> m = resourceService.getById(id);
        if (m == null) return Result.fail(404, "资源不存在");
        return Result.ok(m);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var f = resourceService.create(userId, body);
            return Result.ok(Map.of("id", f.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            resourceService.update(id, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            resourceService.delete(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/download")
    public Result<Map<String, Object>> download(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            return Result.ok(resourceService.download(id, userId));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

