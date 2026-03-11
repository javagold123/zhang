package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.*;
import com.itlichao.backendlabor.mapper.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LoginLogMapper loginLogMapper;
    private final OperationLogMapper operationLogMapper;
    private final ExceptionLogMapper exceptionLogMapper;
    private final SysUserMapper userMapper;

    public LogService(LoginLogMapper loginLogMapper, OperationLogMapper operationLogMapper,
                      ExceptionLogMapper exceptionLogMapper, SysUserMapper userMapper) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.exceptionLogMapper = exceptionLogMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> loginLogs() {
        List<LoginLog> list = loginLogMapper.selectList(
                new LambdaQueryWrapper<LoginLog>().orderByDesc(LoginLog::getCreatedAt).last("LIMIT 200"));
        return list.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("user", userMapper.selectById(l.getUserId()) != null ? userMapper.selectById(l.getUserId()).getName() : l.getUsername());
            m.put("username", l.getUsername());
            m.put("ip", l.getIp());
            m.put("success", l.getSuccess());
            m.put("created_at", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            m.put("time", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> operationLogs() {
        List<OperationLog> list = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreatedAt).last("LIMIT 200"));
        return list.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            SysUser u = userMapper.selectById(l.getUserId());
            m.put("user", u != null ? u.getName() : "未知");
            m.put("action", l.getAction());
            m.put("detail", l.getDetail());
            m.put("created_at", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            m.put("time", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> exceptionLogs() {
        List<ExceptionLog> list = exceptionLogMapper.selectList(
                new LambdaQueryWrapper<ExceptionLog>().orderByDesc(ExceptionLog::getCreatedAt).last("LIMIT 200"));
        return list.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("type", l.getType());
            m.put("message", l.getMessage());
            m.put("created_at", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            m.put("time", l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : null);
            return m;
        }).collect(Collectors.toList());
    }
}
