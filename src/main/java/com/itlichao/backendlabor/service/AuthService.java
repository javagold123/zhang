package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.LoginLog;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.LoginLogMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import com.itlichao.backendlabor.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${server.port:8080}")
    private String port;

    public AuthService(SysUserMapper userMapper, LoginLogMapper loginLogMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            logLogin(null, username, getClientIp(request), 0);
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getDisabled() != null && user.getDisabled() == 1) {
            throw new RuntimeException("账号已禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logLogin(user.getId(), username, getClientIp(request), 0);
            throw new RuntimeException("用户名或密码错误");
        }
        logLogin(user.getId(), username, getClientIp(request), 1);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> userVo = toUserVo(user);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVo);
        return result;
    }

    public Map<String, Object> register(String username, String password, String name, String role,
                                        String studentNo, String phone, String email) {
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (cnt > 0) {
            throw new RuntimeException("用户名已存在");
        }

        String phoneNorm = normalize(phone);
        String emailNorm = normalize(email);
        if (phoneNorm != null && !Patterns.PHONE.matcher(phoneNorm).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (emailNorm != null && !Patterns.EMAIL.matcher(emailNorm).matches()) {
            throw new RuntimeException("邮箱格式不正确");
        }
        if (phoneNorm != null) {
            Long phoneCnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, phoneNorm));
            if (phoneCnt > 0) throw new RuntimeException("手机号已被使用");
        }
        if (emailNorm != null) {
            Long emailCnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, emailNorm));
            if (emailCnt > 0) throw new RuntimeException("邮箱已被使用");
        }

        if (!"student".equals(role) && !"teacher".equals(role)) {
            role = "student";
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name != null ? name : username);
        user.setRole(role);
        user.setStudentNo(studentNo);
        user.setPhone(phoneNorm);
        user.setEmail(emailNorm);
        user.setDisabled(0);
        userMapper.insert(user);
        return toUserVo(user);
    }

    public Map<String, Object> getMe(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return null;
        return toUserVo(user);
    }

    private Map<String, Object> toUserVo(SysUser u) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", u.getId());
        vo.put("username", u.getUsername());
        vo.put("name", u.getName());
        vo.put("role", u.getRole());
        vo.put("studentNo", u.getStudentNo());
        vo.put("phone", u.getPhone());
        vo.put("email", u.getEmail());
        vo.put("avatar", u.getAvatar());
        vo.put("disabled", u.getDisabled());
        return vo;
    }

    private void logLogin(Long userId, String username, String ip, int success) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIp(ip);
        log.setSuccess(success);
        loginLogMapper.insert(log);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
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
