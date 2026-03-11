package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Reservation;
import com.itlichao.backendlabor.entity.ReservationSeries;
import com.itlichao.backendlabor.mapper.ReservationMapper;
import com.itlichao.backendlabor.mapper.ReservationSeriesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecurringReservationService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationSeriesMapper seriesMapper;
    private final ReservationMapper reservationMapper;

    public RecurringReservationService(ReservationSeriesMapper seriesMapper, ReservationMapper reservationMapper) {
        this.seriesMapper = seriesMapper;
        this.reservationMapper = reservationMapper;
    }

    public List<Map<String, Object>> mySeries(Long userId) {
        return seriesMapper.selectList(new LambdaQueryWrapper<ReservationSeries>()
                        .eq(ReservationSeries::getCreatorId, userId)
                        .orderByDesc(ReservationSeries::getCreatedAt))
                .stream().map(this::seriesVo).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createSeries(Long userId, Map<String, Object> body) {
        Long labId = getLong(body, "labId");
        String startDateStr = getStr(body, "startDate");
        String endDateStr = getStr(body, "endDate");
        String weekdays = getStr(body, "weekdays"); // "1,3,5" (Mon=1..Sun=7)
        String startTimeStr = getStr(body, "startTime");
        String endTimeStr = getStr(body, "endTime");
        String purpose = getStr(body, "purpose");

        if (labId == null || startDateStr == null || endDateStr == null || weekdays == null || startTimeStr == null || endTimeStr == null || purpose == null || purpose.isBlank()) {
            throw new RuntimeException("参数不完整");
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        if (endDate.isBefore(startDate)) throw new RuntimeException("结束日期不能早于开始日期");
        LocalTime startTime = LocalTime.parse(startTimeStr, TIME_FMT);
        LocalTime endTime = LocalTime.parse(endTimeStr, TIME_FMT);
        if (!endTime.isAfter(startTime)) throw new RuntimeException("结束时间必须晚于开始时间");

        Set<Integer> weekdaySet = parseWeekdays(weekdays);
        if (weekdaySet.isEmpty()) throw new RuntimeException("请选择周期（星期）");

        ReservationSeries s = new ReservationSeries();
        s.setCreatorId(userId);
        s.setLabId(labId);
        s.setStartDate(startDate);
        s.setEndDate(endDate);
        s.setWeekdaysJson(weekdays);
        s.setStartTime(startTime);
        s.setEndTime(endTime);
        s.setPurpose(purpose.trim());
        s.setRemark(getStr(body, "remark"));
        s.setStatus("active");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        seriesMapper.insert(s);

        int created = 0;
        List<String> conflicts = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            int w = mapDow(d.getDayOfWeek());
            if (!weekdaySet.contains(w)) continue;

            // conflict detect with existing pending/approved
            if (hasConflict(labId, d, startTime, endTime)) {
                conflicts.add(d.toString());
                continue;
            }

            Reservation r = new Reservation();
            r.setLabId(labId);
            r.setUserId(userId);
            r.setSeriesId(s.getId());
            r.setReserveDate(d);
            r.setStartTime(startTime);
            r.setEndTime(endTime);
            r.setPurpose(purpose.trim());
            r.setRemark(s.getRemark());
            r.setStatus("pending");
            reservationMapper.insert(r);
            created++;
        }

        return Map.of(
                "seriesId", s.getId(),
                "createdReservations", created,
                "conflictDates", conflicts
        );
    }

    @Transactional
    public void cancelSeries(Long seriesId, Long userId) {
        ReservationSeries s = seriesMapper.selectById(seriesId);
        if (s == null) return;
        if (!userId.equals(s.getCreatorId())) throw new RuntimeException("无权限");
        s.setStatus("cancelled");
        s.setUpdatedAt(LocalDateTime.now());
        seriesMapper.updateById(s);

        // cancel future pending reservations of this series
        LocalDate today = LocalDate.now();
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeriesId, seriesId)
                .ge(Reservation::getReserveDate, today)
                .in(Reservation::getStatus, "pending", "approved"));
        for (Reservation r : list) {
            r.setStatus("cancelled");
            r.setCancelReason("周期预约取消");
            reservationMapper.updateById(r);
        }
    }

    private boolean hasConflict(Long labId, LocalDate date, LocalTime start, LocalTime end) {
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getLabId, labId)
                .eq(Reservation::getReserveDate, date)
                .in(Reservation::getStatus, "pending", "approved"));
        for (Reservation r : list) {
            LocalTime rs = r.getStartTime(), re = r.getEndTime();
            if (rs == null || re == null) continue;
            if (start.isBefore(re) && end.isAfter(rs)) return true;
        }
        return false;
    }

    private Map<String, Object> seriesVo(ReservationSeries s) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("labId", s.getLabId());
        m.put("startDate", s.getStartDate() != null ? s.getStartDate().toString() : null);
        m.put("endDate", s.getEndDate() != null ? s.getEndDate().toString() : null);
        m.put("weekdays", s.getWeekdaysJson());
        m.put("startTime", s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : null);
        m.put("endTime", s.getEndTime() != null ? s.getEndTime().format(TIME_FMT) : null);
        m.put("purpose", s.getPurpose());
        m.put("remark", s.getRemark());
        m.put("status", s.getStatus());
        m.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString().replace("T", " ") : null);
        return m;
    }

    private static Set<Integer> parseWeekdays(String raw) {
        if (raw == null) return Set.of();
        Set<Integer> set = new HashSet<>();
        for (String p : raw.split(",")) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= 7) set.add(v);
            } catch (NumberFormatException ignored) {}
        }
        return set;
    }

    private static int mapDow(DayOfWeek d) {
        // Java: MONDAY=1..SUNDAY=7 already, but keep explicit
        return d.getValue();
    }

    private static String getStr(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : null;
    }

    private static Long getLong(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}

