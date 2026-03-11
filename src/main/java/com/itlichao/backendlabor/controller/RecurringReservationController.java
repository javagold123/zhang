package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.RecurringReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recurring-reservations")
public class RecurringReservationController {

    private final RecurringReservationService service;

    public RecurringReservationController(RecurringReservationService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> mySeries(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(service.mySeries(userId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            return Result.ok(service.createSeries(userId, body));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            service.cancelSeries(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

