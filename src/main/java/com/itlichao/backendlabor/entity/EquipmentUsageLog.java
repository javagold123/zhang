package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_usage_log")
public class EquipmentUsageLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("user_id")
    private Long userId;

    private String action;
    private String detail;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

