package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.LabService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    private boolean isAdminOrTeacher(HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        if (!(role instanceof String r)) return false;
        return "admin".equals(r) || "teacher".equals(r);
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String building) {
        List<Map<String, Object>> list = labService.list(search, building);
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> lab = labService.getById(id);
        if (lab == null) return Result.fail(404, "实验室不存在");
        return Result.ok(lab);
    }

    @GetMapping("/{id}/occupied")
    public Result<List<String>> getOccupied(@PathVariable Long id, @RequestParam String date) {
        List<String> slots = labService.getOccupiedSlots(id, date);
        return Result.ok(slots);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            var lab = labService.create(body);
            return Result.ok(labService.getById(lab.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            labService.update(id, body);
            return Result.ok(labService.getById(id));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            labService.delete(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/equipment")
    public Result<Void> saveEquipment(@PathVariable Long id, @RequestBody List<Map<String, Object>> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            labService.saveEquipment(id, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/open-time")
    public Result<Void> saveOpenTime(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdminOrTeacher(request)) return Result.fail(403, "无权限");
        try {
            labService.saveOpenTime(id, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
