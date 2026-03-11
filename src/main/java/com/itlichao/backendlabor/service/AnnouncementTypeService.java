package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.AnnouncementType;
import com.itlichao.backendlabor.mapper.AnnouncementTypeMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnnouncementTypeService {

    private final AnnouncementTypeMapper typeMapper;

    public AnnouncementTypeService(AnnouncementTypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public List<Map<String, Object>> list() {
        List<AnnouncementType> list = typeMapper.selectList(new LambdaQueryWrapper<AnnouncementType>().orderByAsc(AnnouncementType::getName));
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public AnnouncementType create(Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) throw new RuntimeException("类型名称不能为空");
        name = name.trim();
        Long cnt = typeMapper.selectCount(new LambdaQueryWrapper<AnnouncementType>().eq(AnnouncementType::getName, name));
        if (cnt != null && cnt > 0) throw new RuntimeException("类型已存在");
        AnnouncementType t = new AnnouncementType();
        t.setName(name);
        typeMapper.insert(t);
        return t;
    }

    public AnnouncementType update(Long id, Map<String, Object> body) {
        AnnouncementType t = typeMapper.selectById(id);
        if (t == null) throw new RuntimeException("类型不存在");
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) throw new RuntimeException("类型名称不能为空");
        name = name.trim();
        Long cnt = typeMapper.selectCount(new LambdaQueryWrapper<AnnouncementType>()
                .eq(AnnouncementType::getName, name)
                .ne(AnnouncementType::getId, id));
        if (cnt != null && cnt > 0) throw new RuntimeException("类型已存在");
        t.setName(name);
        typeMapper.updateById(t);
        return t;
    }

    public void delete(Long id) {
        typeMapper.deleteById(id);
    }

    private Map<String, Object> toVo(AnnouncementType t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        return m;
    }
}

