package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String category;
    private Integer top;
    @TableField("publisher_id")
    private Long publisherId;
    @TableField("publish_at")
    private LocalDateTime publishAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
