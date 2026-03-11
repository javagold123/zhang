package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Lab;
import com.itlichao.backendlabor.entity.Reservation;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.LabMapper;
import com.itlichao.backendlabor.mapper.ReservationMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final LabMapper labMapper;
    private final ReservationMapper reservationMapper;
    private final SysUserMapper userMapper;

    public StatsService(LabMapper labMapper, ReservationMapper reservationMapper, SysUserMapper userMapper) {
        this.labMapper = labMapper;
        this.reservationMapper = reservationMapper;
        this.userMapper = userMapper;
    }

    /** 首页概览统计 */
    public Map<String, Object> homeStats() {
        long labCount = labMapper.selectCount(null);
        LocalDate today = LocalDate.now();
        long todayCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getReserveDate, today)
                        .in(Reservation::getStatus, "pending", "approved"));
        List<Lab> labs = labMapper.selectList(null);
        int totalCapacity = labs.stream().mapToInt(l -> l.getCapacity() != null ? l.getCapacity() : 0).sum();
        Map<String, Object> m = new HashMap<>();
        m.put("labCount", labCount);
        m.put("todayReservationCount", todayCount);
        m.put("totalCapacity", totalCapacity);
        return m;
    }

    /**
     * 统计分析页
     * 如果未传入 start / end，则使用当前周作为默认统计区间；
     * 如果传入了起止日期，则只统计该时间段内的预约记录。
     */
    public Map<String, Object> statistics(LocalDate startDate, LocalDate endDate) {
        List<Lab> labs = labMapper.selectList(new LambdaQueryWrapper<Lab>().eq(Lab::getStatus, "available"));

        LocalDate today = LocalDate.now();
        if (endDate == null) {
            endDate = today;
        }
        if (startDate == null) {
            // 默认按「本周」口径：从本周一开始
            startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
        }
        // 确保 start <= end
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        // 仅在时间区间内的预约
        List<Reservation> all = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .between(Reservation::getReserveDate, startDate, endDate)
        );

        // 参考日期：以统计区间的结束日期为准（用于周、月维度分桶）
        LocalDate refDate = endDate;

        // 按实验室统计
        Map<Long, Long> labCountMap = all.stream()
                .collect(Collectors.groupingBy(Reservation::getLabId, Collectors.counting()));
        Map<Long, String> labIdToName = labs.stream().collect(Collectors.toMap(Lab::getId, Lab::getName, (a, b) -> a));
        List<Map<String, Object>> labUsage = new ArrayList<>();
        long totalCountAllLabs = labCountMap.values().stream().mapToLong(Long::longValue).sum();
        if (totalCountAllLabs <= 0) totalCountAllLabs = 1;
        for (Lab lab : labs) {
            long count = labCountMap.getOrDefault(lab.getId(), 0L);
            int rate = (int) Math.round(count * 100.0 / totalCountAllLabs); // 按预约次数占比
            if (rate > 100) rate = 100;
            Map<String, Object> item = new HashMap<>();
            item.put("name", lab.getName());
            item.put("count", (int) count);
            item.put("rate", rate);
            labUsage.add(item);
        }
        labUsage.sort((a, b) -> ((Integer) b.get("rate")).compareTo((Integer) a.get("rate")));

        // 本周每日（仍然按 refDate 所在周口径，保证默认进入页面为「本周」视角）
        LocalDate weekStart = refDate.minusDays(refDate.getDayOfWeek().getValue() - 1);
        int[] byDay = new int[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            long c = all.stream().filter(r -> d.equals(r.getReserveDate())).count();
            byDay[i] = (int) c;
        }
        int weekTotal = Arrays.stream(byDay).sum();

        // 最近6周
        int[] byWeek = new int[6];
        for (int i = 0; i < 6; i++) {
            LocalDate ws = weekStart.minusWeeks(5 - i);
            LocalDate we = ws.plusDays(6);
            long c = all.stream().filter(r -> {
                LocalDate rd = r.getReserveDate();
                return !rd.isBefore(ws) && !rd.isAfter(we);
            }).count();
            byWeek[i] = (int) c;
        }

        // 最近6个月
        int[] byMonth = new int[6];
        for (int i = 0; i < 6; i++) {
            LocalDate mStart = refDate.minusMonths(5 - i).withDayOfMonth(1);
            final int yy = mStart.getYear();
            final int mm = mStart.getMonthValue();
            long c = all.stream().filter(r -> {
                LocalDate rd = r.getReserveDate();
                return rd.getYear() == yy && rd.getMonthValue() == mm;
            }).count();
            byMonth[i] = (int) c;
        }

        // 近30天趋势（按日）
        List<Map<String, Object>> trendDaily = new ArrayList<>();
        LocalDate start30 = refDate.minusDays(29);
        for (int i = 0; i < 30; i++) {
            LocalDate d = start30.plusDays(i);
            long c = all.stream().filter(r -> d.equals(r.getReserveDate())).count();
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.format(DateTimeFormatter.ISO_DATE));
            item.put("count", (int) c);
            trendDaily.add(item);
        }

        // 预约状态分布（全量）
        Map<String, Integer> statusDist = new LinkedHashMap<>();
        List<String> statuses = List.of("approved", "pending", "rejected", "completed", "cancelled");
        for (String s : statuses) {
            statusDist.put(s, (int) all.stream().filter(r -> s.equals(r.getStatus())).count());
        }

        // 预约开始时间段分布（按小时 8-21）
        List<Map<String, Object>> hourDist = new ArrayList<>();
        for (int h = 8; h <= 21; h++) {
            final int hh = h;
            int c = (int) all.stream().filter(r -> {
                LocalTime st = r.getStartTime();
                return st != null && st.getHour() == hh;
            }).count();
            Map<String, Object> item = new HashMap<>();
            item.put("hour", String.format("%02d:00", h));
            item.put("count", c);
            hourDist.add(item);
        }

        // Top实验室（按次数）
        List<Map<String, Object>> topLabs = labCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("labId", e.getKey());
                    item.put("name", labIdToName.getOrDefault(e.getKey(), "实验室"));
                    item.put("count", e.getValue().intValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 用户排行
        Map<Long, Long> userCountMap = all.stream()
                .collect(Collectors.groupingBy(Reservation::getUserId, Collectors.counting()));
        List<Map<String, Object>> userRank = userCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    SysUser u = userMapper.selectById(e.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", u != null ? u.getName() : "用户");
                    item.put("count", e.getValue().intValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labUsage", labUsage);
        result.put("reservationByDay", Arrays.stream(byDay).boxed().collect(Collectors.toList()));
        result.put("reservationByWeek", Arrays.stream(byWeek).boxed().collect(Collectors.toList()));
        result.put("reservationByMonth", Arrays.stream(byMonth).boxed().collect(Collectors.toList()));
        result.put("trendDaily", trendDaily);
        result.put("statusDist", statusDist);
        result.put("hourDist", hourDist);
        result.put("topLabs", topLabs);
        result.put("userRank", userRank);
        result.put("totalReservations", all.size());
        result.put("weekTotalReservations", weekTotal);
        result.put("referenceDate", refDate.format(DateTimeFormatter.ISO_DATE));
        result.put("labCount", labs.size());
        result.put("avgUsageRate", labUsage.isEmpty() ? 0 : (int) labUsage.stream().mapToInt(m -> (Integer) m.get("rate")).average().orElse(0));
        return result;
    }
}
