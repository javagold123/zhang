package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.ExperimentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    private boolean isTeacherOrAdmin(HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        return role instanceof String r && ("teacher".equals(r) || "admin".equals(r));
    }

    // projects
    @GetMapping("/projects")
    public Result<List<Map<String, Object>>> listProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String category
    ) {
        return Result.ok(experimentService.listProjects(search, category));
    }

    @PostMapping("/projects")
    public Result<Map<String, Object>> createProject(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var p = experimentService.createProject(userId, body);
            return Result.ok(Map.of("id", p.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/projects/{id}")
    public Result<Void> updateProject(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            experimentService.updateProject(id, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            experimentService.deleteProject(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    // plans
    @GetMapping("/plans")
    public Result<List<Map<String, Object>>> myPlans(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(experimentService.myPlans(userId));
    }

    @PostMapping("/plans")
    public Result<Map<String, Object>> createPlan(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var p = experimentService.createPlan(userId, body);
            return Result.ok(Map.of("id", p.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/plans/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            experimentService.updatePlan(id, userId, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/plans/{id}")
    public Result<Void> deletePlan(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            experimentService.deletePlan(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    // reports
    @GetMapping("/reports")
    public Result<List<Map<String, Object>>> myReports(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(experimentService.myReports(userId));
    }

    @PostMapping("/reports")
    public Result<Map<String, Object>> submitReport(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var r = experimentService.submitReport(userId, body);
            return Result.ok(Map.of("id", r.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/reports/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        Long reviewerId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        Double score = body.get("score") instanceof Number n ? n.doubleValue() : null;
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;
        try {
            experimentService.reviewReport(id, reviewerId, score, comment);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    // achievements
    @GetMapping("/achievements")
    public Result<List<Map<String, Object>>> myAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(experimentService.myAchievements(userId));
    }

    @PostMapping("/achievements")
    public Result<Map<String, Object>> createAchievement(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var a = experimentService.createAchievement(userId, body);
            return Result.ok(Map.of("id", a.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

