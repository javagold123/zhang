package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Feedback;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.FeedbackMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FeedbackMapper feedbackMapper;
    private final SysUserMapper userMapper;

    public FeedbackService(FeedbackMapper feedbackMapper, SysUserMapper userMapper) {
        this.feedbackMapper = feedbackMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> list(String status, String type, Long userId) {
        LambdaQueryWrapper<Feedback> q = new LambdaQueryWrapper<>();
        if (userId != null) q.eq(Feedback::getUserId, userId);
        if (status != null && !status.isEmpty() && !"all".equals(status)) q.eq(Feedback::getStatus, status);
        if (type != null && !type.isEmpty() && !"all".equals(type)) q.eq(Feedback::getType, type);
        q.orderByDesc(Feedback::getCreatedAt);
        List<Feedback> list = feedbackMapper.selectList(q);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public Feedback create(Long userId, Map<String, Object> body) {
        Feedback f = new Feedback();
        f.setUserId(userId);
        f.setType((String) body.getOrDefault("type", "建议"));
        f.setTitle((String) body.get("title"));
        f.setContent((String) body.get("content"));
        f.setIsAnonymous(Boolean.TRUE.equals(body.get("isAnonymous")) ? 1 : 0);
        f.setStatus("pending");
        feedbackMapper.insert(f);
        return f;
    }

    private Map<String, Object> toVo(Feedback f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.getId());
        m.put("type", f.getType());
        m.put("title", f.getTitle());
        m.put("content", f.getContent());
        m.put("status", f.getStatus());
        m.put("reply", f.getReply());
        m.put("submitAt", f.getCreatedAt() != null ? f.getCreatedAt().format(FMT) : null);
        SysUser u = f.getIsAnonymous() != null && f.getIsAnonymous() == 1 ? null : userMapper.selectById(f.getUserId());
        m.put("userName", u != null ? u.getName() : "匿名");
        m.put("userNo", u != null ? u.getStudentNo() : "");
        return m;
    }
}
