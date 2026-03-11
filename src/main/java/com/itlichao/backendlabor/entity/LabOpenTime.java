package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("lab_open_time")
public class LabOpenTime {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("lab_id")
    private Long labId;
    @TableField("open_start")
    private LocalTime openStart;
    @TableField("open_end")
    private LocalTime openEnd;
    @TableField("blackout_json")
    private String blackoutJson;
    @TableField("holidays_json")
    private String holidaysJson;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
