package com.itlichao.backendlabor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private String type;
    private String title;
    private String content;
    @TableField("is_anonymous")
    private Integer isAnonymous;
    private String status;
    private String reply;
    @TableField("reply_at")
    private LocalDateTime replyAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
