package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.UserNotification;
import com.itlichao.backendlabor.mapper.UserNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final UserNotificationMapper mapper;

    public NotificationService(UserNotificationMapper mapper) {
        this.mapper = mapper;
    }

    public List<UserNotification> myList(Long userId, String status) {
        LambdaQueryWrapper<UserNotification> q = new LambdaQueryWrapper<>();
        q.eq(UserNotification::getUserId, userId).orderByDesc(UserNotification::getCreatedAt);
        if ("unread".equalsIgnoreCase(status)) {
            q.isNull(UserNotification::getReadAt);
        } else if ("read".equalsIgnoreCase(status)) {
            q.isNotNull(UserNotification::getReadAt);
        }
        return mapper.selectList(q);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        UserNotification n = mapper.selectById(id);
        if (n == null) throw new RuntimeException("消息不存在");
        if (!userId.equals(n.getUserId())) throw new RuntimeException("无权限");
        if (n.getReadAt() != null) return;
        n.setReadAt(LocalDateTime.now());
        mapper.updateById(n);
    }

    @Transactional
    public int markAllRead(Long userId) {
        UserNotification upd = new UserNotification();
        upd.setReadAt(LocalDateTime.now());
        LambdaQueryWrapper<UserNotification> q = new LambdaQueryWrapper<>();
        q.eq(UserNotification::getUserId, userId).isNull(UserNotification::getReadAt);
        return mapper.update(upd, q);
    }

    @Transactional
    public UserNotification createForUser(Long userId, String type, String title, String content) {
        UserNotification n = new UserNotification();
        n.setUserId(userId);
        n.setType(type != null ? type : "system");
        n.setTitle(title != null ? title : "");
        n.setContent(content);
        n.setCreatedAt(LocalDateTime.now());
        mapper.insert(n);
        return n;
    }
}

