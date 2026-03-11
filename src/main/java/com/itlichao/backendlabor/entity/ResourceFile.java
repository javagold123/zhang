package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_file")
public class ResourceFile {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("uploader_id")
    private Long uploaderId;

    private String title;
    private String description;
    private String category;
    private String tags;

    @TableField("file_url")
    private String fileUrl;
    @TableField("file_name")
    private String fileName;
    @TableField("file_size")
    private Long fileSize;
    @TableField("mime_type")
    private String mimeType;

    @TableField("download_count")
    private Integer downloadCount;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

