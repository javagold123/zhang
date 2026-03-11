package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Announcement;
import com.itlichao.backendlabor.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    public List<Map<String, Object>> list() {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getTop, Announcement::getPublishAt));
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public Announcement create(Map<String, Object> body, Long publisherId) {
        Announcement a = new Announcement();
        a.setTitle((String) body.get("title"));
        a.setContent((String) body.get("content"));
        a.setCategory((String) body.get("category"));
        a.setTop(getInt(body, "top", 0) != 0 ? 1 : 0);
        a.setPublisherId(publisherId);
        a.setPublishAt(LocalDateTime.now());
        announcementMapper.insert(a);
        return a;
    }

    public void update(Long id, Map<String, Object> body) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new RuntimeException("公告不存在");
        if (body.get("title") != null) a.setTitle((String) body.get("title"));
        if (body.get("content") != null) a.setContent((String) body.get("content"));
        if (body.get("category") != null) a.setCategory((String) body.get("category"));
        if (body.get("top") != null) a.setTop(getInt(body, "top", 0) != 0 ? 1 : 0);
        a.setPublishAt(LocalDateTime.now());
        announcementMapper.updateById(a);
    }

    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    private Map<String, Object> toVo(Announcement a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("content", a.getContent());
        m.put("category", a.getCategory());
        m.put("top", a.getTop());
        m.put("publishAt", a.getPublishAt() != null ? a.getPublishAt().format(FMT) : null);
        m.put("publish_at", a.getPublishAt() != null ? a.getPublishAt().format(FMT) : null);
        return m;
    }

    private int getInt(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof Boolean) return (Boolean) v ? 1 : 0;
        return def;
    }
}
