package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.ResourceDownloadLog;
import com.itlichao.backendlabor.entity.ResourceFile;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.ResourceDownloadLogMapper;
import com.itlichao.backendlabor.mapper.ResourceFileMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ResourceFileMapper fileMapper;
    private final ResourceDownloadLogMapper downloadLogMapper;
    private final SysUserMapper userMapper;

    public ResourceService(ResourceFileMapper fileMapper, ResourceDownloadLogMapper downloadLogMapper, SysUserMapper userMapper) {
        this.fileMapper = fileMapper;
        this.downloadLogMapper = downloadLogMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> list(String category, String search) {
        LambdaQueryWrapper<ResourceFile> q = new LambdaQueryWrapper<>();
        q.eq(ResourceFile::getStatus, "active").orderByDesc(ResourceFile::getCreatedAt);
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            q.eq(ResourceFile::getCategory, category);
        }
        if (search != null && !search.isBlank()) {
            String s = search.trim();
            q.and(w -> w.like(ResourceFile::getTitle, s)
                    .or().like(ResourceFile::getDescription, s)
                    .or().like(ResourceFile::getTags, s)
                    .or().like(ResourceFile::getFileName, s));
        }
        return fileMapper.selectList(q).stream().map(this::toVo).collect(Collectors.toList());
    }

    public Map<String, Object> getById(Long id) {
        ResourceFile f = fileMapper.selectById(id);
        if (f == null || !"active".equals(f.getStatus())) return null;
        return toVo(f);
    }

    @Transactional
    public ResourceFile create(Long uploaderId, Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String category = body.get("category") != null ? body.get("category").toString() : null;
        if (title == null || title.isBlank()) throw new RuntimeException("标题不能为空");
        if (category == null || category.isBlank()) throw new RuntimeException("分类不能为空");

        ResourceFile f = new ResourceFile();
        f.setUploaderId(uploaderId);
        f.setTitle(title.trim());
        f.setCategory(category.trim());
        f.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        f.setTags(body.get("tags") != null ? body.get("tags").toString() : null);
        f.setFileUrl(body.get("fileUrl") != null ? body.get("fileUrl").toString() : null);
        f.setFileName(body.get("fileName") != null ? body.get("fileName").toString() : null);
        f.setFileSize(body.get("fileSize") instanceof Number n ? n.longValue() : null);
        f.setMimeType(body.get("mimeType") != null ? body.get("mimeType").toString() : null);
        f.setDownloadCount(0);
        f.setStatus("active");
        f.setCreatedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        fileMapper.insert(f);
        return f;
    }

    @Transactional
    public void update(Long id, Map<String, Object> body) {
        ResourceFile f = fileMapper.selectById(id);
        if (f == null || !"active".equals(f.getStatus())) throw new RuntimeException("资源不存在");
        if (body.get("title") != null) f.setTitle(body.get("title").toString().trim());
        if (body.get("description") != null) f.setDescription(body.get("description").toString());
        if (body.get("category") != null) f.setCategory(body.get("category").toString());
        if (body.get("tags") != null) f.setTags(body.get("tags").toString());
        if (body.get("fileUrl") != null) f.setFileUrl(body.get("fileUrl").toString());
        if (body.get("fileName") != null) f.setFileName(body.get("fileName").toString());
        if (body.get("fileSize") instanceof Number n) f.setFileSize(n.longValue());
        if (body.get("mimeType") != null) f.setMimeType(body.get("mimeType").toString());
        f.setUpdatedAt(LocalDateTime.now());
        fileMapper.updateById(f);
    }

    @Transactional
    public void delete(Long id) {
        ResourceFile f = fileMapper.selectById(id);
        if (f == null || !"active".equals(f.getStatus())) return;
        f.setStatus("deleted");
        f.setUpdatedAt(LocalDateTime.now());
        fileMapper.updateById(f);
    }

    @Transactional
    public Map<String, Object> download(Long id, Long userId) {
        ResourceFile f = fileMapper.selectById(id);
        if (f == null || !"active".equals(f.getStatus())) throw new RuntimeException("资源不存在");
        Integer cnt = f.getDownloadCount() != null ? f.getDownloadCount() : 0;
        f.setDownloadCount(cnt + 1);
        f.setUpdatedAt(LocalDateTime.now());
        fileMapper.updateById(f);

        ResourceDownloadLog log = new ResourceDownloadLog();
        log.setResourceId(id);
        log.setUserId(userId);
        log.setCreatedAt(LocalDateTime.now());
        downloadLogMapper.insert(log);

        return Map.of(
                "fileUrl", f.getFileUrl(),
                "fileName", f.getFileName(),
                "downloadCount", f.getDownloadCount()
        );
    }

    private Map<String, Object> toVo(ResourceFile f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.getId());
        m.put("title", f.getTitle());
        m.put("description", f.getDescription());
        m.put("category", f.getCategory());
        m.put("tags", f.getTags());
        m.put("fileUrl", f.getFileUrl());
        m.put("fileName", f.getFileName());
        m.put("fileSize", f.getFileSize());
        m.put("mimeType", f.getMimeType());
        m.put("downloadCount", f.getDownloadCount());
        m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString().replace("T", " ") : null);
        SysUser u = f.getUploaderId() != null ? userMapper.selectById(f.getUploaderId()) : null;
        m.put("uploaderName", u != null ? u.getName() : "");
        return m;
    }
}

