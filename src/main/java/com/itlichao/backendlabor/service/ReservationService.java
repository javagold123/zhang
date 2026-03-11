package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Lab;
import com.itlichao.backendlabor.entity.Reservation;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.LabMapper;
import com.itlichao.backendlabor.mapper.ReservationMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationMapper reservationMapper;
    private final LabMapper labMapper;
    private final SysUserMapper userMapper;

    public ReservationService(ReservationMapper reservationMapper, LabMapper labMapper, SysUserMapper userMapper) {
        this.reservationMapper = reservationMapper;
        this.labMapper = labMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> myList(Long userId, String status) {
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.eq(Reservation::getUserId, userId).orderByDesc(Reservation::getCreatedAt);
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            q.eq(Reservation::getStatus, status);
        }
        List<Reservation> list = reservationMapper.selectList(q);
        return list.stream().map(r -> toVo(r)).collect(Collectors.toList());
    }

    public List<Map<String, Object>> pendingList() {
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.eq(Reservation::getStatus, "pending").orderByAsc(Reservation::getCreatedAt);
        List<Reservation> list = reservationMapper.selectList(q);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public List<Map<String, Object>> historyList() {
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.in(Reservation::getStatus, "approved", "rejected")
                .orderByDesc(Reservation::getApprovedAt);
        List<Reservation> list = reservationMapper.selectList(q);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    public List<Map<String, Object>> calendarList(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.in(Reservation::getStatus, "pending", "approved")
                .ge(Reservation::getReserveDate, start)
                .le(Reservation::getReserveDate, end)
                .orderByAsc(Reservation::getReserveDate, Reservation::getStartTime);
        List<Reservation> list = reservationMapper.selectList(q);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    @Transactional
    public Reservation create(Long userId, Map<String, Object> body) {
        Long labId = getLong(body, "labId");
        String dateStr = (String) body.get("date");
        String startStr = (String) body.get("startTime");
        String endStr = (String) body.get("endTime");
        String purpose = (String) body.get("purpose");
        if (labId == null || dateStr == null || startStr == null || endStr == null || purpose == null || purpose.trim().isEmpty()) {
            throw new RuntimeException("参数不完整");
        }
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime start = LocalTime.parse(startStr, TIME_FMT);
        LocalTime end = LocalTime.parse(endStr, TIME_FMT);
        if (!end.isAfter(start)) throw new RuntimeException("结束时间必须晚于开始时间");
        if (date.isBefore(LocalDate.now())) throw new RuntimeException("不能预约过去日期");
        // 冲突检测
        if (hasConflict(labId, date, start, end, null)) {
            throw new RuntimeException("该时段已被预约");
        }
        Reservation r = new Reservation();
        r.setLabId(labId);
        r.setUserId(userId);
        r.setReserveDate(date);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setPurpose(purpose.trim());
        r.setRemark((String) body.get("remark"));
        r.setStatus("pending");
        reservationMapper.insert(r);
        return r;
    }

    @Transactional
    public void update(Long id, Long userId, Map<String, Object> body) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new RuntimeException("预约不存在");
        if (!r.getUserId().equals(userId)) throw new RuntimeException("无权修改");
        if (!"pending".equals(r.getStatus())) throw new RuntimeException("只能修改待审批的预约");
        String dateStr = (String) body.get("date");
        String startStr = (String) body.get("startTime");
        String endStr = (String) body.get("endTime");
        String purpose = (String) body.get("purpose");
        if (dateStr != null && startStr != null && endStr != null) {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime start = LocalTime.parse(startStr, TIME_FMT);
            LocalTime end = LocalTime.parse(endStr, TIME_FMT);
            if (hasConflict(r.getLabId(), date, start, end, id)) {
                throw new RuntimeException("该时段已被预约");
            }
            r.setReserveDate(date);
            r.setStartTime(start);
            r.setEndTime(end);
        }
        if (purpose != null) r.setPurpose(purpose.trim());
        if (body.get("remark") != null) r.setRemark((String) body.get("remark"));
        reservationMapper.updateById(r);
    }

    @Transactional
    public void cancel(Long id, Long userId, String reason) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new RuntimeException("预约不存在");
        if (!r.getUserId().equals(userId)) throw new RuntimeException("无权取消");
        if ("cancelled".equals(r.getStatus()) || "completed".equals(r.getStatus())) {
            throw new RuntimeException("该预约已不能取消");
        }
        r.setStatus("cancelled");
        r.setCancelReason(reason != null ? reason : "用户取消");
        reservationMapper.updateById(r);
    }

    @Transactional
    public void approve(Long id, Long approverId, boolean approved, String comment) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new RuntimeException("预约不存在");
        if (!"pending".equals(r.getStatus())) throw new RuntimeException("该预约已审批");
        if (approved && hasConflict(r.getLabId(), r.getReserveDate(), r.getStartTime(), r.getEndTime(), id)) {
            throw new RuntimeException("该时段已被预约，无法通过");
        }
        r.setStatus(approved ? "approved" : "rejected");
        r.setApproverId(approverId);
        r.setApproveComment(comment);
        r.setApprovedAt(LocalDateTime.now());
        reservationMapper.updateById(r);
    }

    private boolean hasConflict(Long labId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.eq(Reservation::getLabId, labId).eq(Reservation::getReserveDate, date)
                .in(Reservation::getStatus, "pending", "approved");
        if (excludeId != null) q.ne(Reservation::getId, excludeId);
        List<Reservation> list = reservationMapper.selectList(q);
        for (Reservation r : list) {
            LocalTime rs = r.getStartTime(), re = r.getEndTime();
            if ((start.isBefore(re) && end.isAfter(rs))) return true;
        }
        return false;
    }

    private Long getLong(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String) {
            try {
                return Long.parseLong((String) v);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Map<String, Object> toVo(Reservation r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("labId", r.getLabId());
        m.put("userId", r.getUserId());
        m.put("date", r.getReserveDate() != null ? r.getReserveDate().toString() : null);
        m.put("reserve_date", r.getReserveDate() != null ? r.getReserveDate().toString() : null);
        m.put("startTime", formatTime(r.getStartTime()));
        m.put("endTime", formatTime(r.getEndTime()));
        m.put("purpose", r.getPurpose());
        m.put("remark", r.getRemark());
        m.put("status", r.getStatus());
        m.put("cancelReason", r.getCancelReason());
        m.put("approve_comment", r.getApproveComment());
        m.put("created_at", r.getCreatedAt() != null ? r.getCreatedAt().toString().replace("T", " ") : null);
        m.put("approvedAt", r.getApprovedAt() != null ? r.getApprovedAt().toString().replace("T", " ") : null);
        Lab lab = labMapper.selectById(r.getLabId());
        if (lab != null) {
            m.put("labName", lab.getName());
            m.put("building", lab.getBuilding());
            m.put("room", lab.getRoom());
        }
        SysUser user = userMapper.selectById(r.getUserId());
        if (user != null) {
            m.put("userName", user.getName());
            m.put("studentNo", user.getStudentNo());
        }
        m.put("submitAt", r.getCreatedAt() != null ? r.getCreatedAt().toString().replace("T", " ") : null);
        m.put("result", "approved".equals(r.getStatus()) ? "approved" : ("rejected".equals(r.getStatus()) ? "rejected" : null));
        m.put("comment", r.getApproveComment());
        return m;
    }

    private String formatTime(LocalTime t) {
        return t != null ? t.format(TIME_FMT) : "";
    }
}
