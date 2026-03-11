package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.service.SysConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final SysConfigService configService;

    public ConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public Result<Map<String, Object>> get() {
        return Result.ok(configService.getConfig());
    }

    @PutMapping
    public Result<Void> save(@RequestBody Map<String, Object> body, jakarta.servlet.http.HttpServletRequest request) {
        try {
            configService.saveConfig(body);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
