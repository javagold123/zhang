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
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("lab_id")
    private Long labId;
    @TableField("user_id")
    private Long userId;
    @TableField("group_id")
    private Long groupId;
    @TableField("series_id")
    private Long seriesId;
    @TableField("reserve_date")
    private LocalDate reserveDate;
    @TableField("start_time")
    private LocalTime startTime;
    @TableField("end_time")
    private LocalTime endTime;
    private String purpose;
    private String remark;
    private String status;
    @TableField("cancel_reason")
    private String cancelReason;
    @TableField("approver_id")
    private Long approverId;
    @TableField("approve_comment")
    private String approveComment;
    @TableField("approved_at")
    private LocalDateTime approvedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String labName;
    @TableField(exist = false)
    private String building;
    @TableField(exist = false)
    private String room;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String studentNo;
}
