package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Map<String, Object> updateMe(Long userId, Map<String, Object> body) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return null;
        if (body.get("name") != null) user.setName((String) body.get("name"));
        if (body.get("phone") != null) {
            String phone = normalize((String) body.get("phone"));
            if (phone != null && !Patterns.PHONE.matcher(phone).matches()) {
                throw new RuntimeException("手机号格式不正确");
            }
            user.setPhone(phone);
        }
        if (body.get("email") != null) {
            String email = normalize((String) body.get("email"));
            if (email != null && !Patterns.EMAIL.matcher(email).matches()) {
                throw new RuntimeException("邮箱格式不正确");
            }
            user.setEmail(email);
        }
        if (body.get("avatar") != null) user.setAvatar((String) body.get("avatar"));
        if (body.get("studentNo") != null) user.setStudentNo((String) body.get("studentNo"));
        String newPwd = (String) body.get("password");
        if (newPwd != null && !newPwd.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPwd));
        }
        userMapper.updateById(user);
        return toVo(user);
    }

    public List<Map<String, Object>> list(String search) {
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim().toLowerCase();
            q.and(w -> w.like(SysUser::getName, s).or().like(SysUser::getUsername, s)
                    .or().like(SysUser::getStudentNo, s));
        }
        List<SysUser> list = userMapper.selectList(q);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public void updateRole(Long id, String role) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setRole(role);
        userMapper.updateById(user);
    }

    public void toggleDisabled(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("admin".equals(user.getRole())) throw new RuntimeException("不能禁用管理员");
        user.setDisabled(user.getDisabled() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("admin".equals(user.getRole())) throw new RuntimeException("不能删除管理员");
        userMapper.deleteById(id);
    }

    private Map<String, Object> toVo(SysUser u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("name", u.getName());
        m.put("role", u.getRole());
        m.put("studentNo", u.getStudentNo());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("avatar", u.getAvatar());
        m.put("disabled", u.getDisabled());
        return m;
    }

    private static String normalize(String v) {
        if (v == null) return null;
        String s = v.trim();
        return s.isEmpty() ? null : s;
    }

    private static final class Patterns {
        private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
        private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        private Patterns() {}
    }
}
