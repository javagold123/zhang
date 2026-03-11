package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.LabReview;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.LabReviewMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LabReviewService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LabReviewMapper mapper;
    private final SysUserMapper userMapper;

    public LabReviewService(LabReviewMapper mapper, SysUserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> listByLab(Long labId) {
        List<LabReview> list = mapper.selectList(new LambdaQueryWrapper<LabReview>()
                .eq(LabReview::getLabId, labId)
                .eq(LabReview::getStatus, "normal")
                .orderByDesc(LabReview::getCreatedAt));
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public LabReview create(Long labId, Long userId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) throw new RuntimeException("评分需为 1-5");
        LabReview r = new LabReview();
        r.setLabId(labId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setContent(content != null ? content.trim() : null);
        r.setStatus("normal");
        mapper.insert(r);
        return r;
    }

    public void withdraw(Long id, Long userId) {
        LabReview r = mapper.selectById(id);
        if (r == null) throw new RuntimeException("评价不存在");
        if (!userId.equals(r.getUserId())) throw new RuntimeException("只能撤回自己的评价");
        if (!"normal".equals(r.getStatus())) throw new RuntimeException("该评价已撤回");
        r.setStatus("withdrawn");
        r.setDeletedAt(LocalDateTime.now());
        mapper.updateById(r);
    }

    public void deleteByAdmin(Long id, Long operatorId) {
        LabReview r = mapper.selectById(id);
        if (r == null) throw new RuntimeException("评价不存在");
        if (!"normal".equals(r.getStatus())) throw new RuntimeException("该评价已删除");
        r.setStatus("deleted");
        r.setDeletedBy(operatorId);
        r.setDeletedAt(LocalDateTime.now());
        mapper.updateById(r);
    }

    private Map<String, Object> toVo(LabReview r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("labId", r.getLabId());
        m.put("userId", r.getUserId());
        m.put("rating", r.getRating());
        m.put("content", r.getContent());
        m.put("status", r.getStatus());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(FMT) : null);
        SysUser u = userMapper.selectById(r.getUserId());
        m.put("userName", u != null ? u.getName() : "用户");
        return m;
    }
}

