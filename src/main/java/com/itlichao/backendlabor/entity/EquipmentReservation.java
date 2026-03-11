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
@TableName("equipment_reservation")
public class EquipmentReservation {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("user_id")
    private Long userId;

    @TableField("reserve_date")
    private LocalDate reserveDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    private String purpose;
    private String status;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approved_at")
    private LocalDateTime approvedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

