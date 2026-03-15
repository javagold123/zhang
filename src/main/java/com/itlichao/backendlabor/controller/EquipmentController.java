package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.EquipmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    private boolean isTeacherOrAdmin(HttpServletRequest request) {
        Object role = request.getAttribute(AuthInterceptor.ATTR_USER_ROLE);
        return role instanceof String r && ("teacher".equals(r) || "admin".equals(r));
    }

    // -------- items --------
    @GetMapping("/items")
    public Result<List<Map<String, Object>>> listItems(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String search
    ) {
        return Result.ok(equipmentService.listItems(status, search));
    }

    @PostMapping("/items")
    public Result<Map<String, Object>> createItem(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            var e = equipmentService.createItem(body);
            return Result.ok(Map.of("id", e.getId()));
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            equipmentService.updateItem(id, body);
            return Result.ok(null);
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            equipmentService.deleteItem(id);
            return Result.ok(null);
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    // -------- borrows --------
    @GetMapping("/borrows")
    public Result<List<Map<String, Object>>> listBorrows(
            @RequestParam(required = false, defaultValue = "true") boolean mine,
            @RequestParam(required = false, defaultValue = "all") String status,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        boolean allowAll = isTeacherOrAdmin(request) && !mine;
        return Result.ok(equipmentService.listBorrows(userId, !allowAll, status));
    }

    @PostMapping("/borrows")
    public Result<Map<String, Object>> createBorrow(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var b = equipmentService.createBorrow(userId, body);
            return Result.ok(Map.of("id", b.getId()));
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    @PostMapping("/borrows/{id}/return")
    public Result<Void> returnBorrow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            equipmentService.returnBorrow(id, userId);
            return Result.ok(null);
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    // -------- maintenance --------
    @GetMapping("/maintenance")
    public Result<List<Map<String, Object>>> listMaintenance(@RequestParam(required = false, defaultValue = "all") String status) {
        return Result.ok(equipmentService.listMaintenance(status));
    }

    @PostMapping("/maintenance")
    public Result<Map<String, Object>> createMaintenance(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var m = equipmentService.createMaintenance(userId, body);
            return Result.ok(Map.of("id", m.getId()));
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    @PutMapping("/maintenance/{id}")
    public Result<Void> updateMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isTeacherOrAdmin(request)) return Result.fail(403, "无权限");
        try {
            equipmentService.updateMaintenance(id, body);
            return Result.ok(null);
        } catch (Exception ex) {
            return Result.fail(400, ex.getMessage());
        }
    }

    // -------- usage --------
    @GetMapping("/usage")
    public Result<List<Map<String, Object>>> usage(@RequestParam(required = false) Long equipmentId) {
        return Result.ok(equipmentService.listUsage(equipmentId));
    }
}
