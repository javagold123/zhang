package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.service.LabService;
import com.itlichao.backendlabor.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;
    private final LabService labService;

    public LogController(LogService logService, LabService labService) {
        this.logService = logService;
        this.labService = labService;
    }

    @GetMapping("/login")
    public Result<List<Map<String, Object>>> loginLogs(HttpServletRequest request) {
        return Result.ok(logService.loginLogs());
    }

    @GetMapping("/operation")
    public Result<List<Map<String, Object>>> operationLogs(HttpServletRequest request) {
        return Result.ok(logService.operationLogs());
    }

    @GetMapping("/exception")
    public Result<List<Map<String, Object>>> exceptionLogs(HttpServletRequest request) {
        return Result.ok(logService.exceptionLogs());
    }
}
