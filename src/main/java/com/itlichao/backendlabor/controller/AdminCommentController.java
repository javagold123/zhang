package com.itlichao.backendlabor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.entity.Lab;
import com.itlichao.backendlabor.mapper.LabMapper;
import com.itlichao.backendlabor.service.EquipmentCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final EquipmentCommentService commentService;
    private final LabMapper labMapper;

    public AdminCommentController(EquipmentCommentService commentService, LabMapper labMapper) {
        this.commentService = commentService;
        this.labMapper = labMapper;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> listAll(HttpServletRequest request) {
        List<Lab> labs = labMapper.selectList(null);
        Map<Long, String> labMap = labs.stream().collect(Collectors.toMap(Lab::getId, Lab::getName, (a, b) -> a));
        List<Map<String, Object>> list = commentService.listAllForAdmin(labMap);
        return Result.ok(list);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(com.itlichao.backendlabor.interceptor.AuthInterceptor.ATTR_USER_ID);
        try {
            commentService.delete(id, userId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
