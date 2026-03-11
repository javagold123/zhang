package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.interceptor.AuthInterceptor;
import com.itlichao.backendlabor.service.EquipmentCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs/{labId}/comments")
public class CommentController {

    private final EquipmentCommentService commentService;

    public CommentController(EquipmentCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(@PathVariable Long labId) {
        return Result.ok(commentService.listByLab(labId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@PathVariable Long labId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail(400, "评论内容不能为空");
        }
        Long equipmentId = body.get("equipmentId") != null ? Long.valueOf(body.get("equipmentId").toString()) : null;
        var c = commentService.create(labId, userId, content.trim(), equipmentId);
        return Result.ok(Map.of("id", c.getId(), "content", c.getContent()));
    }

    @DeleteMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long labId, @PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            commentService.withdraw(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long labId, @PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        try {
            commentService.delete(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
