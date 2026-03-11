package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_maintenance")
public class EquipmentMaintenance {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("reporter_id")
    private Long reporterId;

    private String type;
    private String description;
    private String status;
    private String operator;

    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

