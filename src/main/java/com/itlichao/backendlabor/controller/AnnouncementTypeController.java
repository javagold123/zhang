package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.AnnouncementTypeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcement-types")
public class AnnouncementTypeController {

    private final AnnouncementTypeService typeService;

    public AnnouncementTypeController(AnnouncementTypeService typeService) {
        this.typeService = typeService;
    }

    private boolean isAdminOrTeacher(HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        if (!(role instanceof String r)) return false;
        return "admin".equals(r) || "teacher".equals(r);
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(typeService.list());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            var t = typeService.create(body);
            return Result.ok(Map.of("id", t.getId(), "name", t.getName()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            var t = typeService.update(id, body);
            return Result.ok(Map.of("id", t.getId(), "name", t.getName()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            typeService.delete(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

