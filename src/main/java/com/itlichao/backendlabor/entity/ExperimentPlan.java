package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("experiment_plan")
public class ExperimentPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("lab_id")
    private Long labId;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("team_group_id")
    private Long teamGroupId;

    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    private String objectives;
    private String steps;
    private String materials;
    private String safety;
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

