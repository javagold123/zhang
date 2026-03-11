package com.itlichao.backendlabor.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.*;
import com.itlichao.backendlabor.mapper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInit implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final LabMapper labMapper;
    private final LabEquipmentMapper equipmentMapper;
    private final LabOpenTimeMapper openTimeMapper;
    private final AnnouncementMapper announcementMapper;
    private final AnnouncementTypeMapper announcementTypeMapper;
    private final SysConfigMapper configMapper;

    public DataInit(SysUserMapper userMapper, LabMapper labMapper, LabEquipmentMapper equipmentMapper,
                    LabOpenTimeMapper openTimeMapper, AnnouncementMapper announcementMapper, AnnouncementTypeMapper announcementTypeMapper,
                    SysConfigMapper configMapper) {
        this.userMapper = userMapper;
        this.labMapper = labMapper;
        this.equipmentMapper = equipmentMapper;
        this.openTimeMapper = openTimeMapper;
        this.announcementMapper = announcementMapper;
        this.announcementTypeMapper = announcementTypeMapper;
        this.configMapper = configMapper;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPwd = encoder.encode("123456");
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (admin == null) {
            SysUser u = new SysUser();
            u.setUsername("admin");
            u.setPassword(encodedPwd);
            u.setName("管理员");
            u.setRole("admin");
            u.setDisabled(0);
            userMapper.insert(u);
        } else if (admin.getPassword() == null || !encoder.matches("123456", admin.getPassword())) {
            admin.setPassword(encodedPwd);
            userMapper.updateById(admin);
        }
        if (labMapper.selectCount(null) == 0) {
            for (Object[] row : new Object[][]{
                    {"LAB-A01", "计算机实验室 A", "信息楼", "301", 50, "PC、投影", "available", "配备高性能计算机与投影设备，适合上机实验与课程演示。"},
                    {"LAB-A02", "计算机实验室 B", "信息楼", "302", 48, "PC、服务器", "available", "含服务器与网络设备，支持网络与系统类实验。"},
                    {"LAB-B01", "物理实验室", "实验楼", "201", 30, "光学仪器、示波器", "available", "光学与电学实验专用，需提前阅读安全须知。"},
                    {"LAB-B02", "化学实验室", "实验楼", "101", 24, "通风橱", "maintenance", "需通过安全考核后方可预约。"},
                    {"LAB-C01", "电子电路实验室", "工程楼", "401", 40, "示波器、信号源", "available", "电子电路与嵌入式实验。"},
                    {"LAB-A03", "创新实验室", "信息楼", "501", 20, "3D 打印机、开发板", "available", "创新创业与竞赛项目使用。"}
            }) {
                Lab lab = new Lab();
                lab.setCode((String) row[0]);
                lab.setName((String) row[1]);
                lab.setBuilding((String) row[2]);
                lab.setRoom((String) row[3]);
                lab.setCapacity((Integer) row[4]);
                lab.setEquipmentSummary((String) row[5]);
                lab.setStatus((String) row[6]);
                lab.setIntro((String) row[7]);
                labMapper.insert(lab);
            }
            List<Lab> labs = labMapper.selectList(null);
            for (Lab lab : labs) {
                LabOpenTime ot = new LabOpenTime();
                ot.setLabId(lab.getId());
                ot.setOpenStart(java.time.LocalTime.of(8, 0));
                ot.setOpenEnd(java.time.LocalTime.of(22, 0));
                if (lab.getName().contains("物理")) ot.setOpenEnd(java.time.LocalTime.of(18, 0));
                if (lab.getName().contains("化学")) ot.setOpenEnd(java.time.LocalTime.of(17, 0));
                openTimeMapper.insert(ot);
            }
            Lab lab1 = labs.stream().filter(l -> "LAB-A01".equals(l.getCode())).findFirst().orElse(null);
            if (lab1 != null) {
                equipmentMapper.insert(equip(lab1.getId(), "PC", 50, "计算机"));
                equipmentMapper.insert(equip(lab1.getId(), "投影仪", 1, "显示"));
            }
            Lab lab2 = labs.stream().filter(l -> "LAB-A02".equals(l.getCode())).findFirst().orElse(null);
            if (lab2 != null) {
                equipmentMapper.insert(equip(lab2.getId(), "PC", 48, "计算机"));
                equipmentMapper.insert(equip(lab2.getId(), "服务器", 2, "服务器"));
            }
            Lab lab3 = labs.stream().filter(l -> "LAB-B01".equals(l.getCode())).findFirst().orElse(null);
            if (lab3 != null) {
                equipmentMapper.insert(equip(lab3.getId(), "示波器", 10, "测量"));
            }
        }
        if (announcementMapper.selectCount(null) == 0) {
            if (announcementTypeMapper.selectCount(null) == 0) {
                for (String name : new String[]{"系统公告", "放假安排", "设备维护", "规则更新", "安全通知"}) {
                    AnnouncementType t = new AnnouncementType();
                    t.setName(name);
                    announcementTypeMapper.insert(t);
                }
            }
            Announcement a1 = new Announcement();
            a1.setTitle("实验室预约系统上线通知");
            a1.setContent("即日起开放实验室预约功能，请同学们合理预约。");
            a1.setCategory("系统公告");
            a1.setTop(1);
            announcementMapper.insert(a1);
            Announcement a2 = new Announcement();
            a2.setTitle("清明节实验室开放安排");
            a2.setContent("清明节期间部分实验室关闭，请关注预约页面。");
            a2.setCategory("放假安排");
            a2.setTop(0);
            announcementMapper.insert(a2);
        }
        if (configMapper.selectCount(null) == 0) {
            for (Object[] row : new Object[][]{
                    {"max_booking_hours", "4"},
                    {"advance_days", "1"},
                    {"cancel_before_hours", "2"},
                    {"rule_text", "\"请至少提前一天预约；单次不超过4小时；取消需在开始前2小时操作。\""}  // JSON string
            }) {
                SysConfig c = new SysConfig();
                c.setConfigKey((String) row[0]);
                c.setConfigValue((String) row[1]);
                configMapper.insert(c);
            }
        }
    }

    private LabEquipment equip(Long labId, String name, int qty, String type) {
        LabEquipment e = new LabEquipment();
        e.setLabId(labId);
        e.setName(name);
        e.setQuantity(qty);
        e.setType(type);
        e.setStatus("正常");
        return e;
    }
}
