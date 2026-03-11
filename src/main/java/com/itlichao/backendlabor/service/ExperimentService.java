package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.*;
import com.itlichao.backendlabor.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    private final ExperimentProjectMapper projectMapper;
    private final ExperimentPlanMapper planMapper;
    private final ExperimentReportMapper reportMapper;
    private final ExperimentAchievementMapper achievementMapper;
    private final SysUserMapper userMapper;
    private final LabMapper labMapper;

    public ExperimentService(
            ExperimentProjectMapper projectMapper,
            ExperimentPlanMapper planMapper,
            ExperimentReportMapper reportMapper,
            ExperimentAchievementMapper achievementMapper,
            SysUserMapper userMapper,
            LabMapper labMapper
    ) {
        this.projectMapper = projectMapper;
        this.planMapper = planMapper;
        this.reportMapper = reportMapper;
        this.achievementMapper = achievementMapper;
        this.userMapper = userMapper;
        this.labMapper = labMapper;
    }

    // -------- projects --------
    public List<Map<String, Object>> listProjects(String search, String category) {
        LambdaQueryWrapper<ExperimentProject> q = new LambdaQueryWrapper<>();
        q.orderByDesc(ExperimentProject::getCreatedAt);
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            q.eq(ExperimentProject::getCategory, category);
        }
        if (search != null && !search.isBlank()) {
            String s = search.trim();
            q.and(w -> w.like(ExperimentProject::getTitle, s)
                    .or().like(ExperimentProject::getDescription, s)
                    .or().like(ExperimentProject::getCode, s));
        }
        return projectMapper.selectList(q).stream().map(this::projectVo).collect(Collectors.toList());
    }

    public Map<String, Object> getProject(Long id) {
        ExperimentProject p = projectMapper.selectById(id);
        return p != null ? projectVo(p) : null;
    }

    @Transactional
    public ExperimentProject createProject(Long creatorId, Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : null;
        if (title == null || title.isBlank()) throw new RuntimeException("项目标题不能为空");
        ExperimentProject p = new ExperimentProject();
        p.setCreatorId(creatorId);
        p.setCode(body.get("code") != null ? body.get("code").toString() : null);
        p.setTitle(title.trim());
        p.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        p.setCategory(body.get("category") != null ? body.get("category").toString() : null);
        p.setDifficulty(body.get("difficulty") != null ? body.get("difficulty").toString() : "medium");
        p.setDurationMinutes(body.get("durationMinutes") instanceof Number n ? n.intValue() : 120);
        p.setStatus(body.get("status") != null ? body.get("status").toString() : "active");
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        projectMapper.insert(p);
        return p;
    }

    @Transactional
    public void updateProject(Long id, Map<String, Object> body) {
        ExperimentProject p = projectMapper.selectById(id);
        if (p == null) throw new RuntimeException("项目不存在");
        if (body.get("code") != null) p.setCode(body.get("code").toString());
        if (body.get("title") != null) p.setTitle(body.get("title").toString());
        if (body.get("description") != null) p.setDescription(body.get("description").toString());
        if (body.get("category") != null) p.setCategory(body.get("category").toString());
        if (body.get("difficulty") != null) p.setDifficulty(body.get("difficulty").toString());
        if (body.get("durationMinutes") instanceof Number n) p.setDurationMinutes(n.intValue());
        if (body.get("status") != null) p.setStatus(body.get("status").toString());
        p.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
    }

    // -------- plans --------
    public List<Map<String, Object>> myPlans(Long ownerId) {
        return planMapper.selectList(new LambdaQueryWrapper<ExperimentPlan>()
                        .eq(ExperimentPlan::getOwnerId, ownerId)
                        .orderByDesc(ExperimentPlan::getCreatedAt))
                .stream().map(this::planVo).collect(Collectors.toList());
    }

    @Transactional
    public ExperimentPlan createPlan(Long ownerId, Map<String, Object> body) {
        Long projectId = getLong(body, "projectId");
        if (projectId == null) throw new RuntimeException("请选择实验项目");
        ExperimentPlan p = new ExperimentPlan();
        p.setOwnerId(ownerId);
        p.setProjectId(projectId);
        p.setLabId(getLong(body, "labId"));
        p.setTeamGroupId(getLong(body, "teamGroupId"));
        p.setPlanDate(body.get("planDate") != null ? java.time.LocalDate.parse(body.get("planDate").toString()) : null);
        p.setStartTime(body.get("startTime") != null ? java.time.LocalTime.parse(body.get("startTime").toString()) : null);
        p.setEndTime(body.get("endTime") != null ? java.time.LocalTime.parse(body.get("endTime").toString()) : null);
        p.setObjectives(body.get("objectives") != null ? body.get("objectives").toString() : null);
        p.setSteps(body.get("steps") != null ? body.get("steps").toString() : null);
        p.setMaterials(body.get("materials") != null ? body.get("materials").toString() : null);
        p.setSafety(body.get("safety") != null ? body.get("safety").toString() : null);
        p.setStatus(body.get("status") != null ? body.get("status").toString() : "draft");
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(p);
        return p;
    }

    @Transactional
    public void updatePlan(Long id, Long ownerId, Map<String, Object> body) {
        ExperimentPlan p = planMapper.selectById(id);
        if (p == null) throw new RuntimeException("计划不存在");
        if (!ownerId.equals(p.getOwnerId())) throw new RuntimeException("无权限");
        if (body.get("labId") != null) p.setLabId(getLong(body, "labId"));
        if (body.get("teamGroupId") != null) p.setTeamGroupId(getLong(body, "teamGroupId"));
        if (body.get("planDate") != null) p.setPlanDate(java.time.LocalDate.parse(body.get("planDate").toString()));
        if (body.get("startTime") != null) p.setStartTime(java.time.LocalTime.parse(body.get("startTime").toString()));
        if (body.get("endTime") != null) p.setEndTime(java.time.LocalTime.parse(body.get("endTime").toString()));
        if (body.get("objectives") != null) p.setObjectives(body.get("objectives").toString());
        if (body.get("steps") != null) p.setSteps(body.get("steps").toString());
        if (body.get("materials") != null) p.setMaterials(body.get("materials").toString());
        if (body.get("safety") != null) p.setSafety(body.get("safety").toString());
        if (body.get("status") != null) p.setStatus(body.get("status").toString());
        p.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(p);
    }

    @Transactional
    public void deletePlan(Long id, Long ownerId) {
        ExperimentPlan p = planMapper.selectById(id);
        if (p == null) return;
        if (!ownerId.equals(p.getOwnerId())) throw new RuntimeException("无权限");
        planMapper.deleteById(id);
    }

    // -------- reports --------
    public List<Map<String, Object>> myReports(Long userId) {
        return reportMapper.selectList(new LambdaQueryWrapper<ExperimentReport>()
                        .eq(ExperimentReport::getAuthorId, userId)
                        .orderByDesc(ExperimentReport::getCreatedAt))
                .stream().map(this::reportVo).collect(Collectors.toList());
    }

    @Transactional
    public ExperimentReport submitReport(Long userId, Map<String, Object> body) {
        Long planId = getLong(body, "planId");
        if (planId == null) throw new RuntimeException("缺少 planId");
        String title = body.get("title") != null ? body.get("title").toString() : null;
        ExperimentReport r = new ExperimentReport();
        r.setPlanId(planId);
        r.setAuthorId(userId);
        r.setTitle(title != null ? title : "实验报告");
        r.setContent(body.get("content") != null ? body.get("content").toString() : null);
        r.setConclusion(body.get("conclusion") != null ? body.get("conclusion").toString() : null);
        r.setStatus("submitted");
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        reportMapper.insert(r);
        return r;
    }

    @Transactional
    public void reviewReport(Long id, Long reviewerId, Double score, String comment) {
        ExperimentReport r = reportMapper.selectById(id);
        if (r == null) throw new RuntimeException("报告不存在");
        r.setScore(score);
        r.setReviewerId(reviewerId);
        r.setReviewedAt(LocalDateTime.now());
        r.setStatus("reviewed");
        if (comment != null && !comment.isBlank()) {
            r.setConclusion((r.getConclusion() == null ? "" : r.getConclusion() + "\n") + "评语：" + comment.trim());
        }
        r.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(r);
    }

    // -------- achievements --------
    public List<Map<String, Object>> myAchievements(Long ownerId) {
        return achievementMapper.selectList(new LambdaQueryWrapper<ExperimentAchievement>()
                        .eq(ExperimentAchievement::getOwnerId, ownerId)
                        .orderByDesc(ExperimentAchievement::getCreatedAt))
                .stream().map(this::achievementVo).collect(Collectors.toList());
    }

    @Transactional
    public ExperimentAchievement createAchievement(Long ownerId, Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : null;
        if (title == null || title.isBlank()) throw new RuntimeException("成果标题不能为空");
        ExperimentAchievement a = new ExperimentAchievement();
        a.setOwnerId(ownerId);
        a.setProjectId(getLong(body, "projectId"));
        a.setTitle(title.trim());
        a.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        a.setEvidenceUrl(body.get("evidenceUrl") != null ? body.get("evidenceUrl").toString() : null);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        achievementMapper.insert(a);
        return a;
    }

    // -------- vo helpers --------
    private Map<String, Object> projectVo(ExperimentProject p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("code", p.getCode());
        m.put("title", p.getTitle());
        m.put("description", p.getDescription());
        m.put("category", p.getCategory());
        m.put("difficulty", p.getDifficulty());
        m.put("durationMinutes", p.getDurationMinutes());
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T", " ") : null);
        if (p.getCreatorId() != null) {
            SysUser u = userMapper.selectById(p.getCreatorId());
            m.put("creatorName", u != null ? u.getName() : "");
        }
        return m;
    }

    private Map<String, Object> planVo(ExperimentPlan p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("projectId", p.getProjectId());
        m.put("labId", p.getLabId());
        m.put("teamGroupId", p.getTeamGroupId());
        m.put("planDate", p.getPlanDate() != null ? p.getPlanDate().toString() : null);
        m.put("startTime", p.getStartTime() != null ? p.getStartTime().toString() : null);
        m.put("endTime", p.getEndTime() != null ? p.getEndTime().toString() : null);
        m.put("objectives", p.getObjectives());
        m.put("steps", p.getSteps());
        m.put("materials", p.getMaterials());
        m.put("safety", p.getSafety());
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T", " ") : null);
        ExperimentProject proj = projectMapper.selectById(p.getProjectId());
        m.put("projectTitle", proj != null ? proj.getTitle() : "");
        if (p.getLabId() != null) {
            Lab lab = labMapper.selectById(p.getLabId());
            m.put("labName", lab != null ? lab.getName() : "");
        }
        return m;
    }

    private Map<String, Object> reportVo(ExperimentReport r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("planId", r.getPlanId());
        m.put("authorId", r.getAuthorId());
        m.put("title", r.getTitle());
        m.put("content", r.getContent());
        m.put("conclusion", r.getConclusion());
        m.put("score", r.getScore());
        m.put("status", r.getStatus());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString().replace("T", " ") : null);
        SysUser a = userMapper.selectById(r.getAuthorId());
        m.put("authorName", a != null ? a.getName() : "");
        if (r.getReviewerId() != null) {
            SysUser u = userMapper.selectById(r.getReviewerId());
            m.put("reviewerName", u != null ? u.getName() : "");
        }
        return m;
    }

    private Map<String, Object> achievementVo(ExperimentAchievement a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("projectId", a.getProjectId());
        m.put("title", a.getTitle());
        m.put("description", a.getDescription());
        m.put("evidenceUrl", a.getEvidenceUrl());
        m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString().replace("T", " ") : null);
        if (a.getProjectId() != null) {
            ExperimentProject p = projectMapper.selectById(a.getProjectId());
            m.put("projectTitle", p != null ? p.getTitle() : "");
        }
        return m;
    }

    private Long getLong(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}

