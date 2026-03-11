package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itlichao.backendlabor.entity.SysConfig;
import com.itlichao.backendlabor.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SysConfigService {

    private final SysConfigMapper configMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SysConfigService(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public Map<String, Object> getConfig() {
        Map<String, Object> out = new HashMap<>();
        String[] keys = {"max_booking_hours", "advance_days", "cancel_before_hours", "rule_text"};
        for (String key : keys) {
            SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (c != null && c.getConfigValue() != null) {
                try {
                    Object v = objectMapper.readValue(c.getConfigValue(), Object.class);
                    if (v instanceof Number) {
                        out.put(key, ((Number) v).intValue());
                    } else {
                        out.put(key, v.toString());
                    }
                } catch (Exception e) {
                    out.put(key, c.getConfigValue());
                }
            }
        }
        out.putIfAbsent("max_booking_hours", 4);
        out.putIfAbsent("advance_days", 1);
        out.putIfAbsent("cancel_before_hours", 2);
        out.putIfAbsent("rule_text", "请至少提前一天预约；单次不超过4小时；取消需在开始前2小时操作。");
        // 前端驼峰
        out.put("maxBookingHours", out.get("max_booking_hours"));
        out.put("advanceDays", out.get("advance_days"));
        out.put("cancelBeforeHours", out.get("cancel_before_hours"));
        out.put("ruleText", out.get("rule_text"));
        return out;
    }

    public void saveConfig(Map<String, Object> body) {
        ObjectMapper om = new ObjectMapper();
        for (Map.Entry<String, Object> e : body.entrySet()) {
            String key = toSnake(e.getKey());
            if (!key.equals("max_booking_hours") && !key.equals("advance_days")
                    && !key.equals("cancel_before_hours") && !key.equals("rule_text")) continue;
            try {
                String val = om.writeValueAsString(e.getValue());
                SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
                if (c == null) {
                    c = new SysConfig();
                    c.setConfigKey(key);
                    c.setConfigValue(val);
                    configMapper.insert(c);
                } else {
                    c.setConfigValue(val);
                    configMapper.updateById(c);
                }
            } catch (Exception ex) {
                throw new RuntimeException("保存配置失败");
            }
        }
    }

    private String toSnake(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
