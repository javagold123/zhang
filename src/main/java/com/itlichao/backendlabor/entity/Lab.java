package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("lab")
public class Lab {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String building;
    private String room;
    private Integer capacity;
    @TableField("equipment_summary")
    private String equipmentSummary;
    private String status;
    private String intro;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<LabEquipment> equipmentList;
    @TableField(exist = false)
    private LabOpenTime openTime;
}
