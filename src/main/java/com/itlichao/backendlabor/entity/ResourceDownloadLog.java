package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_download_log")
public class ResourceDownloadLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("user_id")
    private Long userId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

