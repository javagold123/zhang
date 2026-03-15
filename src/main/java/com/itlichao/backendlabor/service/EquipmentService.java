package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.Equipment;
import com.itlichao.backendlabor.entity.EquipmentBorrow;
import com.itlichao.backendlabor.entity.EquipmentMaintenance;
import com.itlichao.backendlabor.entity.EquipmentUsageLog;
import com.itlichao.backendlabor.entity.Lab;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.EquipmentBorrowMapper;
import com.itlichao.backendlabor.mapper.EquipmentMaintenanceMapper;
import com.itlichao.backendlabor.mapper.EquipmentMapper;
import com.itlichao.backendlabor.mapper.EquipmentUsageLogMapper;
import com.itlichao.backendlabor.mapper.LabMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final EquipmentMapper equipmentMapper;
    private final EquipmentBorrowMapper borrowMapper;
    private final EquipmentMaintenanceMapper maintenanceMapper;
    private final EquipmentUsageLogMapper usageLogMapper;
    private final LabMapper labMapper;
    private final SysUserMapper userMapper;

    public EquipmentService(
            EquipmentMapper equipmentMapper,
            EquipmentBorrowMapper borrowMapper,
            EquipmentMaintenanceMapper maintenanceMapper,
            EquipmentUsageLogMapper usageLogMapper,
            LabMapper labMapper,
            SysUserMapper userMapper
    ) {
        this.equipmentMapper = equipmentMapper;
        this.borrowMapper = borrowMapper;
        this.maintenanceMapper = maintenanceMapper;
        this.usageLogMapper = usageLogMapper;
        this.labMapper = labMapper;
        this.userMapper = userMapper;
    }

    // -------- equipment items --------
    public List<Map<String, Object>> listItems(String status, String search) {
        LambdaQueryWrapper<Equipment> q = new LambdaQueryWrapper<>();
        q.orderByDesc(Equipment::getUpdatedAt);
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) q.eq(Equipment::getStatus, status);
        if (search != null && !search.isBlank()) {
            String s = search.trim();
            q.and(w -> w.like(Equipment::getName, s).or().like(Equipment::getType, s).or().like(Equipment::getModel, s).or().like(Equipment::getAssetNo, s));
        }
        return equipmentMapper.selectList(q).stream().map(this::itemVo).collect(Collectors.toList());
    }

    @Transactional
    public Equipment createItem(Map<String, Object> body) {
        String name = getStr(body, "name");
        if (name == null || name.isBlank()) throw new RuntimeException("设备名称不能为空");
        Equipment e = new Equipment();
        e.setAssetNo(getStr(body, "assetNo"));
        e.setName(name.trim());
        e.setType(getStr(body, "type"));
        e.setModel(getStr(body, "model"));
        e.setStatus(getStr(body, "status") != null ? getStr(body, "status") : "available");
        e.setQuantity(body.get("quantity") instanceof Number n ? n.intValue() : 1);
        e.setRemaining(body.get("remaining") instanceof Number r ? r.intValue() : e.getQuantity());
        e.setLocation(getStr(body, "location"));
        e.setNote(getStr(body, "note"));
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.insert(e);
        log(e.getId(), null, "create", "新增设备：" + e.getName());
        return e;
    }

    @Transactional
    public void updateItem(Long id, Map<String, Object> body) {
        Equipment e = equipmentMapper.selectById(id);
        if (e == null) throw new RuntimeException("设备不存在");
        if (body.get("assetNo") != null) e.setAssetNo(getStr(body, "assetNo"));
        if (body.get("name") != null) e.setName(getStr(body, "name"));
        if (body.get("type") != null) e.setType(getStr(body, "type"));
        if (body.get("model") != null) e.setModel(getStr(body, "model"));
        if (body.get("status") != null) e.setStatus(getStr(body, "status"));
        if (body.get("quantity") instanceof Number n) e.setQuantity(n.intValue());
        if (body.get("remaining") instanceof Number r) e.setRemaining(r.intValue());
        if (body.get("location") != null) e.setLocation(getStr(body, "location"));
        if (body.get("note") != null) e.setNote(getStr(body, "note"));
        e.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.updateById(e);
        log(e.getId(), null, "update", "更新设备：" + e.getName());
    }

    @Transactional
    public void deleteItem(Long id) {
        equipmentMapper.deleteById(id);
        log(id, null, "delete", "删除设备#" + id);
    }

    // -------- borrow --------
    public List<Map<String, Object>> listBorrows(Long userId, boolean mine, String status) {
        LambdaQueryWrapper<EquipmentBorrow> q = new LambdaQueryWrapper<>();
        if (mine) q.eq(EquipmentBorrow::getUserId, userId);
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) q.eq(EquipmentBorrow::getStatus, status);
        q.orderByDesc(EquipmentBorrow::getCreatedAt);
        return borrowMapper.selectList(q).stream().map(this::borrowVo).collect(Collectors.toList());
    }

    @Transactional
    public EquipmentBorrow createBorrow(Long userId, Map<String, Object> body) {
        Long equipmentId = getLong(body, "equipmentId");
        if (equipmentId == null) throw new RuntimeException("缺少 equipmentId");
        EquipmentBorrow b = new EquipmentBorrow();
        b.setEquipmentId(equipmentId);
        b.setUserId(userId);
        b.setBorrowAt(LocalDateTime.now());
        b.setDueAt(parseDateTime(getStr(body, "dueAt")));
        b.setPurpose(getStr(body, "purpose"));
        b.setStatus("borrowing");
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        borrowMapper.insert(b);
        log(equipmentId, userId, "borrow", "借用设备#" + equipmentId);
        return b;
    }

    @Transactional
    public void returnBorrow(Long id, Long userId) {
        EquipmentBorrow b = borrowMapper.selectById(id);
        if (b == null) throw new RuntimeException("记录不存在");
        if (!userId.equals(b.getUserId())) throw new RuntimeException("无权限");
        if (!"borrowing".equals(b.getStatus())) throw new RuntimeException("该记录已归还");
        b.setStatus("returned");
        b.setReturnAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        borrowMapper.updateById(b);
        log(b.getEquipmentId(), userId, "return", "归还借用记录#" + id);
    }

    // -------- maintenance --------
    public List<Map<String, Object>> listMaintenance(String status) {
        LambdaQueryWrapper<EquipmentMaintenance> q = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) q.eq(EquipmentMaintenance::getStatus, status);
        q.orderByDesc(EquipmentMaintenance::getCreatedAt);
        return maintenanceMapper.selectList(q).stream().map(this::maintenanceVo).collect(Collectors.toList());
    }

    @Transactional
    public EquipmentMaintenance createMaintenance(Long reporterId, Map<String, Object> body) {
        Long equipmentId = getLong(body, "equipmentId");
        String desc = getStr(body, "description");
        if (equipmentId == null || desc == null || desc.isBlank()) throw new RuntimeException("参数不完整");
        EquipmentMaintenance m = new EquipmentMaintenance();
        m.setEquipmentId(equipmentId);
        m.setReporterId(reporterId);
        m.setType(getStr(body, "type") != null ? getStr(body, "type") : "repair");
        m.setDescription(desc.trim());
        m.setStatus("processing");
        m.setOperator(getStr(body, "operator"));
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        maintenanceMapper.insert(m);
        log(equipmentId, reporterId, "maintenance", "提交维修#" + m.getId());
        return m;
    }

    @Transactional
    public void updateMaintenance(Long id, Map<String, Object> body) {
        EquipmentMaintenance m = maintenanceMapper.selectById(id);
        if (m == null) throw new RuntimeException("记录不存在");
        if (body.get("type") != null) m.setType(getStr(body, "type"));
        if (body.get("description") != null) m.setDescription(getStr(body, "description"));
        if (body.get("status") != null) {
            String st = getStr(body, "status");
            m.setStatus(st);
            if ("completed".equals(st)) m.setResolvedAt(LocalDateTime.now());
        }
        if (body.get("operator") != null) m.setOperator(getStr(body, "operator"));
        m.setUpdatedAt(LocalDateTime.now());
        maintenanceMapper.updateById(m);
        log(m.getEquipmentId(), null, "maintenance_update", "更新维修#" + id);
    }

    // -------- usage logs --------
    public List<Map<String, Object>> listUsage(Long equipmentId) {
        LambdaQueryWrapper<EquipmentUsageLog> q = new LambdaQueryWrapper<>();
        if (equipmentId != null) q.eq(EquipmentUsageLog::getEquipmentId, equipmentId);
        q.orderByDesc(EquipmentUsageLog::getCreatedAt);
        return usageLogMapper.selectList(q).stream().map(this::usageVo).collect(Collectors.toList());
    }

    private void log(Long equipmentId, Long userId, String action, String detail) {
        if (equipmentId == null) return;
        EquipmentUsageLog l = new EquipmentUsageLog();
        l.setEquipmentId(equipmentId);
        l.setUserId(userId);
        l.setAction(action);
        l.setDetail(detail);
        l.setCreatedAt(LocalDateTime.now());
        usageLogMapper.insert(l);
    }

    // -------- vo --------
    private Map<String, Object> itemVo(Equipment e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("assetNo", e.getAssetNo());
        m.put("name", e.getName());
        m.put("type", e.getType());
        m.put("model", e.getModel());
        m.put("status", e.getStatus());
        m.put("quantity", e.getQuantity());
        m.put("remaining", e.getRemaining());
        m.put("location", e.getLocation());
        m.put("note", e.getNote());
        m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString().replace("T", " ") : null);
        return m;
    }

    private Map<String, Object> borrowVo(EquipmentBorrow b) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId());
        m.put("equipmentId", b.getEquipmentId());
        m.put("userId", b.getUserId());
        m.put("borrowAt", b.getBorrowAt() != null ? b.getBorrowAt().toString().replace("T", " ") : null);
        m.put("dueAt", b.getDueAt() != null ? b.getDueAt().toString().replace("T", " ") : null);
        m.put("returnAt", b.getReturnAt() != null ? b.getReturnAt().toString().replace("T", " ") : null);
        m.put("purpose", b.getPurpose());
        m.put("status", b.getStatus());
        Equipment e = equipmentMapper.selectById(b.getEquipmentId());
        m.put("equipName", e != null ? e.getName() : "");
        SysUser u = userMapper.selectById(b.getUserId());
        m.put("userName", u != null ? u.getName() : "");
        return m;
    }

    private Map<String, Object> maintenanceVo(EquipmentMaintenance x) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", x.getId());
        m.put("equipmentId", x.getEquipmentId());
        m.put("type", x.getType());
        m.put("description", x.getDescription());
        m.put("status", x.getStatus());
        m.put("operator", x.getOperator());
        m.put("resolvedAt", x.getResolvedAt() != null ? x.getResolvedAt().toString().replace("T", " ") : null);
        m.put("createdAt", x.getCreatedAt() != null ? x.getCreatedAt().toString().replace("T", " ") : null);
        Equipment e = equipmentMapper.selectById(x.getEquipmentId());
        m.put("equipName", e != null ? e.getName() : "");
        if (x.getReporterId() != null) {
            SysUser u = userMapper.selectById(x.getReporterId());
            m.put("reporter", u != null ? u.getName() : "");
        }
        return m;
    }

    private Map<String, Object> usageVo(EquipmentUsageLog l) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", l.getId());
        m.put("equipmentId", l.getEquipmentId());
        m.put("userId", l.getUserId());
        m.put("action", l.getAction());
        m.put("detail", l.getDetail());
        m.put("createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString().replace("T", " ") : null);
        if (l.getUserId() != null) {
            SysUser u = userMapper.selectById(l.getUserId());
            m.put("userName", u != null ? u.getName() : "");
        }
        return m;
    }

    private static String getStr(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : null;
    }

    private static Long getLongCast(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static Integer getIntCast(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static Long getLong(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s.replace(" ", "T"));
    }
}

