package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.*;
import com.itlichao.backendlabor.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LabService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final LabMapper labMapper;
    private final LabEquipmentMapper equipmentMapper;
    private final EquipmentMapper equipmentItemMapper;
    private final LabOpenTimeMapper openTimeMapper;
    private final ReservationMapper reservationMapper;

    public LabService(LabMapper labMapper, LabEquipmentMapper equipmentMapper, EquipmentMapper equipmentItemMapper,
                      LabOpenTimeMapper openTimeMapper, ReservationMapper reservationMapper) {
        this.labMapper = labMapper;
        this.equipmentMapper = equipmentMapper;
        this.equipmentItemMapper = equipmentItemMapper;
        this.openTimeMapper = openTimeMapper;
        this.reservationMapper = reservationMapper;
    }

    public List<Map<String, Object>> list(String search, String building) {
        LambdaQueryWrapper<Lab> q = new LambdaQueryWrapper<>();
        q.eq(Lab::getStatus, "available").or().eq(Lab::getStatus, "maintenance").or().eq(Lab::getStatus, "disabled");
        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim().toLowerCase();
            q.and(w -> w.like(Lab::getName, s).or().like(Lab::getBuilding, s).or().like(Lab::getRoom, s));
        }
        if (building != null && !building.isEmpty()) {
            q.eq(Lab::getBuilding, building);
        }
        q.orderByAsc(Lab::getCode);
        List<Lab> labs = labMapper.selectList(q);
        return labs.stream().map(this::toVo).collect(Collectors.toList());
    }

    public Map<String, Object> getById(Long id) {
        Lab lab = labMapper.selectById(id);
        if (lab == null) return null;
        Map<String, Object> vo = toVo(lab);
        List<LabEquipment> equipments = equipmentMapper.selectList(
                new LambdaQueryWrapper<LabEquipment>().eq(LabEquipment::getLabId, id));
        vo.put("equipmentList", equipments.stream().map(this::equipToMap).collect(Collectors.toList()));
        vo.put("equipmentIds", equipments.stream().map(LabEquipment::getEquipmentId).filter(Objects::nonNull).collect(Collectors.toList()));
        Map<Long, Integer> qtyMap = new HashMap<>();
        for (LabEquipment e : equipments) {
            if (e.getEquipmentId() != null) {
                qtyMap.put(e.getEquipmentId(), e.getQuantity() != null ? e.getQuantity() : 0);
            }
        }
        vo.put("equipmentQuantities", qtyMap);
        LabOpenTime ot = openTimeMapper.selectOne(new LambdaQueryWrapper<LabOpenTime>().eq(LabOpenTime::getLabId, id));
        vo.put("openTime", openTimeToMap(ot));
        return vo;
    }

    public List<Map<String, Object>> listAllEquipment() {
        List<LabEquipment> list = equipmentMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LabEquipment e : list) {
            Lab lab = labMapper.selectById(e.getLabId());
            Map<String, Object> m = equipToMap(e);
            m.put("labId", e.getLabId());
            m.put("labName", lab != null ? lab.getName() : "");
            m.put("model", "");
            m.put("purchaseDate", "");
            m.put("warranty", "");
            result.add(m);
        }
        return result;
    }

    public List<String> getOccupiedSlots(Long labId, String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return Collections.emptyList();
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        LambdaQueryWrapper<Reservation> q = new LambdaQueryWrapper<>();
        q.eq(Reservation::getLabId, labId).eq(Reservation::getReserveDate, date)
                .in(Reservation::getStatus, "pending", "approved");
        List<Reservation> list = reservationMapper.selectList(q);
        List<String> slots = new ArrayList<>();
        for (Reservation r : list) {
            slots.add(formatTime(r.getStartTime()) + "-" + formatTime(r.getEndTime()));
        }
        return slots;
    }

    @Transactional
    public Lab create(Map<String, Object> body) {
        String code = (String) body.get("code");
        if (code == null || code.trim().isEmpty()) throw new RuntimeException("实验室编号不能为空");
        Long cnt = labMapper.selectCount(new LambdaQueryWrapper<Lab>().eq(Lab::getCode, code.trim()));
        if (cnt > 0) throw new RuntimeException("编号已存在");
        Lab lab = new Lab();
        fillLab(lab, body);
        labMapper.insert(lab);
        saveOpenTime(lab.getId(), body);
        return lab;
    }

    @Transactional
    public Lab update(Long id, Map<String, Object> body) {
        Lab lab = labMapper.selectById(id);
        if (lab == null) throw new RuntimeException("实验室不存在");
        fillLab(lab, body);
        labMapper.updateById(lab);
        saveOpenTime(id, body);
        return lab;
    }

    @Transactional
    public void delete(Long id) {
        // 若存在未完成预约，禁止删除，避免预约记录变成“孤儿”
        Long pendingCnt = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getLabId, id)
                        .in(Reservation::getStatus, "pending", "approved")
        );
        if (pendingCnt != null && pendingCnt > 0) {
            throw new RuntimeException("该实验室存在未完成预约，无法删除");
        }

        // 先删关联数据，再删实验室
        equipmentMapper.delete(new LambdaQueryWrapper<LabEquipment>().eq(LabEquipment::getLabId, id));
        openTimeMapper.delete(new LambdaQueryWrapper<LabOpenTime>().eq(LabOpenTime::getLabId, id));
        labMapper.deleteById(id);

        recalcEquipmentRemaining();
    }

    @Transactional
    public void saveEquipment(Long labId, List<Map<String, Object>> list) {
        equipmentMapper.delete(new LambdaQueryWrapper<LabEquipment>().eq(LabEquipment::getLabId, labId));
        if (list != null) {
            for (Map<String, Object> m : list) {
                LabEquipment e = new LabEquipment();
                e.setLabId(labId);
                e.setEquipmentId(getLong(m, "equipmentId"));
                e.setName((String) m.get("name"));
                e.setQuantity(getInt(m, "quantity", 1));
                e.setType((String) m.getOrDefault("type", "设备"));
                e.setStatus((String) m.getOrDefault("status", "正常"));
                equipmentMapper.insert(e);
            }
        }
        recalcEquipmentRemaining();
    }

    @Transactional
    public void saveOpenTime(Long labId, Map<String, Object> body) {
        LabOpenTime ot = openTimeMapper.selectOne(new LambdaQueryWrapper<LabOpenTime>().eq(LabOpenTime::getLabId, labId));
        if (ot == null) ot = new LabOpenTime();
        ot.setLabId(labId);
        ot.setOpenStart(parseTime((String) body.get("start"), "08:00"));
        ot.setOpenEnd(parseTime((String) body.get("end"), "22:00"));
        List<?> blackout = (List<?>) body.get("blackout");
        ot.setBlackoutJson(blackout != null ? toJson(blackout) : "[]");
        List<?> holidays = (List<?>) body.get("holidays");
        ot.setHolidaysJson(holidays != null ? toJson(holidays) : "[]");
        if (ot.getId() != null) {
            openTimeMapper.updateById(ot);
        } else {
            openTimeMapper.insert(ot);
        }
    }

    private void fillLab(Lab lab, Map<String, Object> body) {
        lab.setCode((String) body.get("code"));
        lab.setName((String) body.get("name"));
        lab.setBuilding((String) body.get("building"));
        lab.setRoom((String) body.get("room"));
        lab.setCapacity(getInt(body, "capacity", 50));
        lab.setEquipmentSummary((String) body.get("equipment"));
        lab.setStatus((String) body.getOrDefault("status", "available"));
        lab.setIntro((String) body.get("intro"));
        lab.setCover((String) body.get("cover"));
    }

    private Map<String, Object> toVo(Lab lab) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", lab.getId());
        m.put("code", lab.getCode());
        m.put("name", lab.getName());
        m.put("building", lab.getBuilding());
        m.put("room", lab.getRoom());
        m.put("capacity", lab.getCapacity());
        m.put("equipment_summary", lab.getEquipmentSummary());
        m.put("equipment", lab.getEquipmentSummary());
        m.put("status", lab.getStatus());
        m.put("intro", lab.getIntro());
        m.put("cover", lab.getCover());
        return m;
    }

    private Map<String, Object> equipToMap(LabEquipment e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName());
        m.put("quantity", e.getQuantity());
        m.put("type", e.getType());
        m.put("status", e.getStatus());
        return m;
    }

    private Map<String, Object> openTimeToMap(LabOpenTime ot) {
        Map<String, Object> m = new HashMap<>();
        if (ot == null) {
            m.put("start", "08:00");
            m.put("end", "22:00");
            m.put("blackout", Collections.emptyList());
            m.put("holidays", Collections.emptyList());
        } else {
            m.put("start", formatTime(ot.getOpenStart()));
            m.put("end", formatTime(ot.getOpenEnd()));
            m.put("blackout", parseJsonArray(ot.getBlackoutJson()));
            m.put("holidays", parseJsonArray(ot.getHolidaysJson()));
        }
        return m;
    }

    private String formatTime(LocalTime t) {
        return t != null ? t.format(TIME_FMT) : "08:00";
    }

    private LocalTime parseTime(String s, String def) {
        if (s == null || s.isEmpty()) return LocalTime.parse(def, TIME_FMT);
        try {
            return LocalTime.parse(s, TIME_FMT);
        } catch (Exception e) {
            return LocalTime.parse(def, TIME_FMT);
        }
    }

    private int getInt(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try {
                return Integer.parseInt((String) v);
            } catch (NumberFormatException ignored) {}
        }
        return def;
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

    private String toJson(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Object o = list.get(i);
            if (o instanceof String) sb.append("\"").append(o.toString().replace("\"", "\\\"")).append("\"");
            else sb.append(o);
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> parseJsonArray(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();
        s = s.trim();
        if (!s.startsWith("[")) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        String inner = s.substring(1, s.length() - 1).trim();
        if (inner.isEmpty()) return out;
        for (String part : inner.split(",")) {
            part = part.trim().replaceAll("^\"|\"$", "");
            if (!part.isEmpty()) out.add(part);
        }
        return out;
    }

    @Transactional
    public void recalcEquipmentRemaining() {
        // 1) 汇总每个 equipment_id 被分配的总量
        Map<Long, Integer> assigned = new HashMap<>();
        List<LabEquipment> allocations = equipmentMapper.selectList(new LambdaQueryWrapper<LabEquipment>().isNotNull(LabEquipment::getEquipmentId));
        for (LabEquipment le : allocations) {
            Long equipmentId = le.getEquipmentId();
            if (equipmentId == null) continue;
            int qty = le.getQuantity() != null ? le.getQuantity() : 0;
            assigned.put(equipmentId, assigned.getOrDefault(equipmentId, 0) + qty);
        }

        // 2) 更新 equipment.remaining，并校验不允许超分配
        List<Equipment> items = equipmentItemMapper.selectList(null);
        for (Equipment e : items) {
            int total = e.getQuantity() != null ? e.getQuantity() : 0;
            int used = assigned.getOrDefault(e.getId(), 0);
            int remaining = total - used;
            if (remaining < 0) {
                throw new RuntimeException("设备【" + e.getName() + "】分配数量超出总量（总量=" + total + ", 已分配=" + used + "）");
            }
            e.setRemaining(remaining);
            equipmentItemMapper.updateById(e);
        }
    }
}
