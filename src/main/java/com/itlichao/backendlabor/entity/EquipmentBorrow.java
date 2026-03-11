package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_borrow")
public class EquipmentBorrow {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("user_id")
    private Long userId;

    @TableField("borrow_at")
    private LocalDateTime borrowAt;

    @TableField("due_at")
    private LocalDateTime dueAt;

    @TableField("return_at")
    private LocalDateTime returnAt;

    private String purpose;
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

