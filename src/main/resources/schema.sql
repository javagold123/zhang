-- 高校实验室预约管理系统 - 建表脚本
-- MySQL 8+

CREATE DATABASE IF NOT EXISTS laboratory_reservation DEFAULT CHARSET utf8mb4;
USE laboratory_reservation;

-- 重建（可重复执行）：先按依赖逆序删除表
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS user_notification;
DROP TABLE IF EXISTS resource_download_log;
DROP TABLE IF EXISTS resource_file;
DROP TABLE IF EXISTS reservation_group_member;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_group;
DROP TABLE IF EXISTS reservation_series;
DROP TABLE IF EXISTS experiment_report;
DROP TABLE IF EXISTS experiment_plan;
DROP TABLE IF EXISTS experiment_achievement;
DROP TABLE IF EXISTS experiment_project;
DROP TABLE IF EXISTS equipment_usage_log;
DROP TABLE IF EXISTS equipment_maintenance;
DROP TABLE IF EXISTS equipment_borrow;
DROP TABLE IF EXISTS equipment_comment;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS lab_review;
DROP TABLE IF EXISTS exception_log;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS login_log;
DROP TABLE IF EXISTS sys_config;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS announcement_type;
DROP TABLE IF EXISTS lab_open_time;
DROP TABLE IF EXISTS lab_equipment;
DROP TABLE IF EXISTS lab;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(64),
  role VARCHAR(20) NOT NULL DEFAULT 'student',
  student_no VARCHAR(32),
  phone VARCHAR(20),
  email VARCHAR(128),
  avatar VARCHAR(512),
  disabled TINYINT(1) NOT NULL DEFAULT 0,
  last_login_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role (role)
) ENGINE=InnoDB;

-- 2. 实验室表
CREATE TABLE IF NOT EXISTS lab (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  building VARCHAR(64),
  room VARCHAR(32),
  capacity INT DEFAULT 0,
  equipment_summary VARCHAR(512),
  status VARCHAR(20) DEFAULT 'available',
  intro TEXT,
  cover VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_building (building)
) ENGINE=InnoDB;

-- 3. 实验室设备表
CREATE TABLE IF NOT EXISTS lab_equipment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_id BIGINT NOT NULL,
  equipment_id BIGINT,
  name VARCHAR(64) NOT NULL,
  quantity INT DEFAULT 1,
  type VARCHAR(32),
  status VARCHAR(20) DEFAULT '正常',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE,
  INDEX idx_lab_id (lab_id),
  INDEX idx_equipment_id (equipment_id)
) ENGINE=InnoDB;

-- 4. 实验室开放时间表
CREATE TABLE IF NOT EXISTS lab_open_time (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_id BIGINT NOT NULL UNIQUE,
  open_start TIME DEFAULT '08:00:00',
  open_end TIME DEFAULT '22:00:00',
  blackout_json TEXT,
  holidays_json TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. 预约表
CREATE TABLE IF NOT EXISTS reservation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  group_id BIGINT,
  series_id BIGINT,
  reserve_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  purpose VARCHAR(512),
  remark VARCHAR(256),
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  cancel_reason VARCHAR(256),
  approver_id BIGINT,
  approve_comment VARCHAR(256),
  approved_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (approver_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_lab_date (lab_id, reserve_date),
  INDEX idx_user_status (user_id, status),
  INDEX idx_group_id (group_id),
  INDEX idx_series_id (series_id),
  INDEX idx_status (status)
) ENGINE=InnoDB;

-- 5.1 团队（用于团队预约）
CREATE TABLE IF NOT EXISTS reservation_group (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  owner_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_owner (owner_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservation_group_member (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(20) DEFAULT 'member',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (group_id) REFERENCES reservation_group(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  UNIQUE KEY uk_group_user (group_id, user_id),
  INDEX idx_group (group_id),
  INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- 5.2 周期预约（系列）
CREATE TABLE IF NOT EXISTS reservation_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  creator_id BIGINT NOT NULL,
  lab_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  weekdays_json VARCHAR(64) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  purpose VARCHAR(512) NOT NULL,
  remark VARCHAR(256),
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (creator_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE,
  INDEX idx_creator (creator_id),
  INDEX idx_lab (lab_id)
) ENGINE=InnoDB;

-- 外键追加：避免建表顺序导致 referenced table 不存在
-- 约束名使用表前缀 reservation_，避免与库中其他表约束重名（防止 Duplicate foreign key constraint name）
ALTER TABLE reservation
  ADD CONSTRAINT reservation_fk_group_id
    FOREIGN KEY (group_id) REFERENCES reservation_group(id) ON DELETE SET NULL,
  ADD CONSTRAINT reservation_fk_series_id
    FOREIGN KEY (series_id) REFERENCES reservation_series(id) ON DELETE SET NULL;

-- 6. 公告表
CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  content TEXT,
  category VARCHAR(64),
  top TINYINT(1) DEFAULT 0,
  publisher_id BIGINT,
  publish_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (publisher_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_top (top),
  INDEX idx_publish_at (publish_at),
  INDEX idx_category (category)
) ENGINE=InnoDB;

-- 6.1 公告类型表
CREATE TABLE IF NOT EXISTS announcement_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_name (name)
) ENGINE=InnoDB;

-- 7. 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_key VARCHAR(64) NOT NULL UNIQUE,
  config_value TEXT,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 8. 设备评论表
CREATE TABLE IF NOT EXISTS equipment_comment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_id BIGINT NOT NULL,
  equipment_id BIGINT,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'normal',
  deleted_by BIGINT,
  deleted_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (deleted_by) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_lab_status (lab_id, status),
  INDEX idx_equipment_id (equipment_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- 8.1 实验室评价表（实验室/设备评价统一承载，可做“实验室评价”模块）
CREATE TABLE IF NOT EXISTS lab_review (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lab_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL DEFAULT 5,
  content TEXT,
  status VARCHAR(20) DEFAULT 'normal',
  deleted_by BIGINT,
  deleted_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (deleted_by) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_lab_created (lab_id, created_at),
  INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- 15. 实验管理：实验项目/计划/报告/成果
CREATE TABLE IF NOT EXISTS experiment_project (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) UNIQUE,
  title VARCHAR(256) NOT NULL,
  description TEXT,
  category VARCHAR(64),
  difficulty VARCHAR(20) DEFAULT 'medium',
  duration_minutes INT DEFAULT 120,
  status VARCHAR(20) DEFAULT 'active',
  creator_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (creator_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_category (category),
  INDEX idx_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS experiment_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  lab_id BIGINT,
  owner_id BIGINT NOT NULL,
  team_group_id BIGINT,
  plan_date DATE,
  start_time TIME,
  end_time TIME,
  objectives TEXT,
  steps TEXT,
  materials TEXT,
  safety TEXT,
  status VARCHAR(20) DEFAULT 'draft',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES experiment_project(id) ON DELETE CASCADE,
  FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE SET NULL,
  FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (team_group_id) REFERENCES reservation_group(id) ON DELETE SET NULL,
  INDEX idx_owner (owner_id),
  INDEX idx_project (project_id),
  INDEX idx_date (plan_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS experiment_report (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  plan_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  title VARCHAR(256),
  content TEXT,
  conclusion TEXT,
  score DECIMAL(5,2),
  reviewer_id BIGINT,
  reviewed_at DATETIME,
  status VARCHAR(20) DEFAULT 'submitted',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (plan_id) REFERENCES experiment_plan(id) ON DELETE CASCADE,
  FOREIGN KEY (author_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_plan (plan_id),
  INDEX idx_author (author_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS experiment_achievement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT,
  owner_id BIGINT NOT NULL,
  title VARCHAR(256) NOT NULL,
  description TEXT,
  evidence_url VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES experiment_project(id) ON DELETE SET NULL,
  FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_owner (owner_id)
) ENGINE=InnoDB;

-- 16. 设备管理：设备列表/预约/借用/维修/使用记录
CREATE TABLE IF NOT EXISTS equipment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_no VARCHAR(64) UNIQUE,
  name VARCHAR(128) NOT NULL,
  type VARCHAR(64),
  model VARCHAR(128),
  status VARCHAR(20) DEFAULT 'available',
  quantity INT DEFAULT 1,
  remaining INT DEFAULT 1,
  location VARCHAR(128),
  note VARCHAR(256),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_type (type)
) ENGINE=InnoDB;

ALTER TABLE equipment_comment
  ADD CONSTRAINT fk_equipment_comment_equipment_id
    FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE SET NULL;


CREATE TABLE IF NOT EXISTS equipment_borrow (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  equipment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  borrow_at DATETIME NOT NULL,
  due_at DATETIME,
  return_at DATETIME,
  purpose VARCHAR(512),
  status VARCHAR(20) DEFAULT 'borrowing',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_equip (equipment_id),
  INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS equipment_maintenance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  equipment_id BIGINT NOT NULL,
  reporter_id BIGINT,
  type VARCHAR(32) DEFAULT 'repair',
  description TEXT,
  status VARCHAR(20) DEFAULT 'processing',
  operator VARCHAR(64),
  resolved_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
  FOREIGN KEY (reporter_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_equip_status (equipment_id, status),
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS equipment_usage_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  equipment_id BIGINT NOT NULL,
  user_id BIGINT,
  action VARCHAR(32) NOT NULL,
  detail VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_equip_created (equipment_id, created_at),
  INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- 17. 资源共享：资料/视频/模板/下载
CREATE TABLE IF NOT EXISTS resource_file (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uploader_id BIGINT,
  title VARCHAR(256) NOT NULL,
  description TEXT,
  category VARCHAR(32) NOT NULL,
  tags VARCHAR(256),
  file_url VARCHAR(512),
  file_name VARCHAR(256),
  file_size BIGINT,
  mime_type VARCHAR(128),
  download_count INT DEFAULT 0,
  status VARCHAR(20) DEFAULT 'active',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (uploader_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_category (category),
  INDEX idx_status (status),
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS resource_download_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  user_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (resource_id) REFERENCES resource_file(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_res_created (resource_id, created_at)
) ENGINE=InnoDB;

-- 9. 登录日志表
CREATE TABLE IF NOT EXISTS login_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(64),
  ip VARCHAR(64),
  success TINYINT(1) DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  action VARCHAR(64),
  detail VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- 11. 意见反馈表
CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type VARCHAR(32) DEFAULT '建议',
  title VARCHAR(128) NOT NULL,
  content TEXT,
  is_anonymous TINYINT(1) DEFAULT 0,
  status VARCHAR(20) DEFAULT 'pending',
  reply TEXT,
  reply_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_status (status),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- 12. 异常日志表
CREATE TABLE IF NOT EXISTS exception_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(64),
  message TEXT,
  stack TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- 13. 消息通知（通知中心）
CREATE TABLE IF NOT EXISTS user_notification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type VARCHAR(32) DEFAULT 'system',
  title VARCHAR(128) NOT NULL,
  content TEXT,
  read_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_user_created (user_id, created_at),
  INDEX idx_user_read (user_id, read_at)
) ENGINE=InnoDB;

-- 14. 角色权限（为后续细粒度权限预留；当前仍兼容 sys_user.role）
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE,
  UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB;
