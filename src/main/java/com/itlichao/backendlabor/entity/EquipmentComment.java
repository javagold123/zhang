package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_comment")
public class EquipmentComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("lab_id")
    private Long labId;
    @TableField("equipment_id")
    private Long equipmentId;
    @TableField("user_id")
    private Long userId;
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

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String labName;
}
