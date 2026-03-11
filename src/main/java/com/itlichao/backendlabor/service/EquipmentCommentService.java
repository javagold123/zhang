package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.EquipmentComment;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.EquipmentCommentMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipmentCommentService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EquipmentCommentMapper commentMapper;
    private final SysUserMapper userMapper;

    public EquipmentCommentService(EquipmentCommentMapper commentMapper, SysUserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> listByLab(Long labId) {
        List<EquipmentComment> list = commentMapper.selectList(
                new LambdaQueryWrapper<EquipmentComment>()
                        .eq(EquipmentComment::getLabId, labId)
                        .eq(EquipmentComment::getStatus, "normal")
                        .orderByDesc(EquipmentComment::getCreatedAt));
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listAllForAdmin(Map<Long, String> labIdToName) {
        List<EquipmentComment> list = commentMapper.selectList(
                new LambdaQueryWrapper<EquipmentComment>()
                        .eq(EquipmentComment::getStatus, "normal")
                        .orderByDesc(EquipmentComment::getCreatedAt));
        return list.stream().map(c -> {
            Map<String, Object> m = toVo(c);
            m.put("labName", labIdToName.getOrDefault(c.getLabId(), "未知"));
            return m;
        }).collect(Collectors.toList());
    }

    public EquipmentComment create(Long labId, Long userId, String content, Long equipmentId) {
        EquipmentComment c = new EquipmentComment();
        c.setLabId(labId);
        c.setEquipmentId(equipmentId);
        c.setUserId(userId);
        c.setContent(content);
        c.setStatus("normal");
        commentMapper.insert(c);
        return c;
    }

    public void withdraw(Long commentId, Long userId) {
        EquipmentComment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("只能撤回自己的评论");
        if (!"normal".equals(c.getStatus())) throw new RuntimeException("该评论已撤回");
        c.setStatus("withdrawn");
        c.setDeletedAt(LocalDateTime.now());
        commentMapper.updateById(c);
    }

    public void delete(Long commentId, Long operatorId) {
        EquipmentComment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!"normal".equals(c.getStatus())) throw new RuntimeException("该评论已删除");
        c.setStatus("deleted");
        c.setDeletedBy(operatorId);
        c.setDeletedAt(LocalDateTime.now());
        commentMapper.updateById(c);
    }

    private Map<String, Object> toVo(EquipmentComment c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("labId", c.getLabId());
        m.put("equipmentId", c.getEquipmentId());
        m.put("userId", c.getUserId());
        m.put("content", c.getContent());
        m.put("status", c.getStatus());
        m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : null);
        SysUser u = userMapper.selectById(c.getUserId());
        m.put("userName", u != null ? u.getName() : "用户");
        return m;
    }
}
