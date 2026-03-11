package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.TeamReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamReservationService teamService;

    public TeamController(TeamReservationService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> myTeams(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return Result.ok(teamService.myGroups(userId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String name = body.get("name") != null ? body.get("name").toString() : null;
        try {
            var g = teamService.createGroup(userId, name);
            return Result.ok(Map.of("id", g.getId()));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> rename(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String name = body.get("name") != null ? body.get("name").toString() : null;
        try {
            teamService.renameGroup(id, userId, name);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            teamService.deleteGroup(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/members")
    public Result<List<Map<String, Object>>> members(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            return Result.ok(teamService.members(id, userId));
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/members")
    public Result<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String username = body.get("username") != null ? body.get("username").toString() : null;
        try {
            teamService.addMemberByUsername(id, userId, username);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId, HttpServletRequest request) {
        Long ownerId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            teamService.removeMember(id, ownerId, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

