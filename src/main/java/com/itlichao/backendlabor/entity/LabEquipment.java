package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_equipment")
public class LabEquipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("lab_id")
    private Long labId;
    @TableField("equipment_id")
    private Long equipmentId;
    private String name;
    private Integer quantity;
    private String type;
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
