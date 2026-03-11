package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("experiment_report")
public class ExperimentReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("author_id")
    private Long authorId;

    private String title;
    private String content;
    private String conclusion;
    private Double score;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

