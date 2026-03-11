package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(announcementService.list());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        if (!(role instanceof String r) || (!"admin".equals(r) && !"teacher".equals(r))) {
            return Result.fail(403, "仅管理员或教师可以发布公告");
        }
        Long publisherId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var a = announcementService.create(body, publisherId);
            return Result.ok(Map.of("id", a.getId(), "title", a.getTitle()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        if (!(role instanceof String r) || (!"admin".equals(r) && !"teacher".equals(r))) {
            return Result.fail(403, "仅管理员或教师可以修改公告");
        }
        try {
            announcementService.update(id, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        if (!(role instanceof String r) || (!"admin".equals(r) && !"teacher".equals(r))) {
            return Result.fail(403, "仅管理员或教师可以删除公告");
        }
        try {
            announcementService.delete(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
