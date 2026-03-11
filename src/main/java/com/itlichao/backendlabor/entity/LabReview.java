package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_review")
public class LabReview {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("lab_id")
    private Long labId;

    @TableField("user_id")
    private Long userId;

    private Integer rating;
    private String content;
    private String status;

    @TableField("deleted_by")
    private Long deletedBy;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

