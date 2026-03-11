package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exception_log")
public class ExceptionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String message;
    private String stack;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
