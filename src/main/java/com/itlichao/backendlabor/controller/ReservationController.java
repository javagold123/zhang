package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> myList(
            @RequestParam(required = false, defaultValue = "all") String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        List<Map<String, Object>> list = reservationService.myList(userId, status);
        return Result.ok(list);
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending(HttpServletRequest request) {
        List<Map<String, Object>> list = reservationService.pendingList();
        return Result.ok(list);
    }

    @GetMapping("/calendar")
    public Result<List<Map<String, Object>>> calendar(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {
        List<Map<String, Object>> list = reservationService.calendarList(year, month);
        return Result.ok(list);
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> history(HttpServletRequest request) {
        List<Map<String, Object>> list = reservationService.historyList();
        return Result.ok(list);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            var r = reservationService.create(userId, body);
            return Result.ok(reservationService.myList(userId, null).stream()
                    .filter(m -> r.getId().equals(m.get("id"))).findFirst().orElse(null));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            reservationService.update(id, userId, body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String reason = body != null ? body.get("reason") : null;
        try {
            reservationService.cancel(id, userId, reason);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        Long approverId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            reservationService.approve(id, approverId, true, null);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        Long approverId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String comment = body != null ? body.get("comment") : null;
        try {
            reservationService.approve(id, approverId, false, comment);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
