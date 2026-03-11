package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("experiment_project")
public class ExperimentProject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String title;
    private String description;
    private String category;
    private String difficulty;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    private String status;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

