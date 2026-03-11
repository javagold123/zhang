-- 高校实验室预约管理系统 - 初始化数据脚本
-- 注意：严格按外键依赖顺序插入，尽量不依赖 FOREIGN_KEY_CHECKS=0

USE laboratory_reservation;

-- 如需重复执行，可先清空（先子表后父表）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE user_notification;
TRUNCATE TABLE resource_download_log;
TRUNCATE TABLE resource_file;
TRUNCATE TABLE equipment_usage_log;
TRUNCATE TABLE equipment_maintenance;
TRUNCATE TABLE equipment_borrow;
TRUNCATE TABLE equipment_reservation;
TRUNCATE TABLE equipment_comment;
TRUNCATE TABLE lab_review;
TRUNCATE TABLE equipment;
TRUNCATE TABLE experiment_report;
TRUNCATE TABLE experiment_plan;
TRUNCATE TABLE experiment_achievement;
TRUNCATE TABLE experiment_project;
TRUNCATE TABLE feedback;
TRUNCATE TABLE operation_log;
TRUNCATE TABLE login_log;
TRUNCATE TABLE reservation_group_member;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_group;
TRUNCATE TABLE reservation_series;
TRUNCATE TABLE lab_open_time;
TRUNCATE TABLE lab_equipment;
TRUNCATE TABLE sys_config;
TRUNCATE TABLE announcement;
TRUNCATE TABLE announcement_type;
TRUNCATE TABLE lab;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE exception_log;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) 根表：用户
INSERT INTO sys_user
  (id, username, password, name, role, student_no, phone, email, avatar, disabled, last_login_at, created_at, updated_at)
VALUES
  (1, 'admin',   '$2a$10$admin-placeholder',   '系统管理员', 'admin',   NULL,       '13800000000', 'admin@example.com',   NULL, 0, NOW(), NOW(), NOW()),
  (2, 'teacher', '$2a$10$teacher-placeholder', '张老师',     'teacher', NULL,       '13800000001', 'teacher@example.com', NULL, 0, NOW(), NOW(), NOW()),
  (3, 'student', '$2a$10$student-placeholder', '李同学',     'student', '20260001', '13800000002', 'student@example.com', NULL, 0, NOW(), NOW(), NOW());

-- 2) 根表：实验室
INSERT INTO lab
  (id, code, name, building, room, capacity, equipment_summary, status, intro, created_at, updated_at)
VALUES
  (1, 'LAB-A101', '化学实验室 A101', '一号楼', 'A101', 30, '通风橱、离心机、显微镜', 'available', '适合基础化学实验与样品处理', NOW(), NOW()),
  (2, 'LAB-B203', '物理实验室 B203', '二号楼', 'B203', 40, '示波器、信号源、万用表', 'available', '适合电路与信号测量实验', NOW(), NOW());

-- 3) 依赖 lab：实验室设备（lab_equipment）
INSERT INTO lab_equipment
  (id, lab_id, name, quantity, type, status, created_at, updated_at)
VALUES
  (1, 1, '通风橱', 4, '安全设备', '正常', NOW(), NOW()),
  (2, 1, '离心机', 2, '仪器',     '正常', NOW(), NOW()),
  (3, 2, '示波器', 6, '仪器',     '正常', NOW(), NOW());

-- 4) 依赖 lab：实验室开放时间（lab_open_time，lab_id 唯一）
INSERT INTO lab_open_time
  (id, lab_id, open_start, open_end, blackout_json, holidays_json, created_at, updated_at)
VALUES
  (1, 1, '08:00:00', '22:00:00', '[]', '[]', NOW(), NOW()),
  (2, 2, '08:30:00', '21:30:00', '[]', '[]', NOW(), NOW());

-- 5) 公告类型（独立表）
INSERT INTO announcement_type (id, name, created_at, updated_at)
VALUES
  (1, '系统通知', NOW(), NOW()),
  (2, '实验室公告', NOW(), NOW()),
  (3, '安全提示', NOW(), NOW());

-- 6) 依赖 sys_user：公告（announcement.publisher_id 可为空）
INSERT INTO announcement
  (id, title, content, category, top, publisher_id, publish_at, created_at, updated_at)
VALUES
  (1, '系统上线通知', '高校实验室预约管理系统已上线试运行。', '系统通知', 1, 1, NOW(), NOW(), NOW()),
  (2, '安全培训提醒', '进入化学实验室前请完成安全培训并佩戴防护用品。', '安全提示', 0, 2, NOW(), NOW(), NOW());

-- 7) 系统配置（独立表）
INSERT INTO sys_config (id, config_key, config_value, updated_at)
VALUES
  (1, 'site.name', '高校实验室预约管理系统', NOW()),
  (2, 'reservation.max_days_ahead', '14', NOW()),
  (3, 'lab.default_open_start', '08:00:00', NOW()),
  (4, 'lab.default_open_end', '22:00:00', NOW());

-- 8) 依赖 sys_user：团队（reservation_group）与成员（reservation_group_member）
INSERT INTO reservation_group (id, name, owner_id, created_at, updated_at)
VALUES
  (1, '化学实验小组', 3, NOW(), NOW());

INSERT INTO reservation_group_member (id, group_id, user_id, role, created_at)
VALUES
  (1, 1, 3, 'owner',  NOW()),
  (2, 1, 2, 'mentor', NOW());

-- 9) 依赖 sys_user、lab：周期预约（reservation_series）
INSERT INTO reservation_series
  (id, creator_id, lab_id, start_date, end_date, weekdays_json, start_time, end_time, purpose, remark, status, created_at, updated_at)
VALUES
  (1, 3, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 28 DAY), '[1,3,5]', '14:00:00', '16:00:00',
   '化学实验课程：样品制备', '每周一三五下午', 'active', NOW(), NOW());

-- 10) 依赖 lab、sys_user、reservation_group、reservation_series：预约（reservation）
INSERT INTO reservation
  (id, lab_id, user_id, group_id, series_id, reserve_date, start_time, end_time, purpose, remark, status,
   cancel_reason, approver_id, approve_comment, approved_at, created_at, updated_at)
VALUES
  (1, 1, 3, 1, 1, CURDATE(), '14:00:00', '16:00:00', '样品制备与离心', '带齐防护用品', 'approved',
   NULL, 2, '注意安全与通风', NOW(), NOW(), NOW()),
  (2, 2, 3, NULL, NULL, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '12:00:00', '电路测量与示波器使用', NULL, 'pending',
   NULL, NULL, NULL, NULL, NOW(), NOW());

-- 11) 实验管理：项目 -> 计划 -> 报告/成果
INSERT INTO experiment_project
  (id, code, title, description, category, difficulty, duration_minutes, status, creator_id, created_at, updated_at)
VALUES
  (1, 'EXP-CH-001', '基础离心实验', '学习离心机的安全操作与样品处理流程', '化学', 'easy', 120, 'active', 2, NOW(), NOW());

INSERT INTO experiment_plan
  (id, project_id, lab_id, owner_id, team_group_id, plan_date, start_time, end_time, objectives, steps, materials, safety, status, created_at, updated_at)
VALUES
  (1, 1, 1, 3, 1, CURDATE(), '14:00:00', '16:00:00',
   '掌握离心机使用、完成样品分离', '1) 准备样品 2) 设置参数 3) 运行与记录', '离心管、样品、标签', '佩戴护目镜与手套，遵守通风要求', 'submitted', NOW(), NOW());

INSERT INTO experiment_report
  (id, plan_id, author_id, title, content, conclusion, score, reviewer_id, reviewed_at, status, created_at, updated_at)
VALUES
  (1, 1, 3, '基础离心实验报告', '记录参数、现象与数据。', '样品分离成功，需进一步优化转速与时间。', 92.50, 2, NOW(), 'submitted', NOW(), NOW());

INSERT INTO experiment_achievement
  (id, project_id, owner_id, title, description, evidence_url, created_at, updated_at)
VALUES
  (1, 1, 3, '样品分离结果', '完成样品分离并整理实验数据', NULL, NOW(), NOW());

-- 12) 设备管理：设备 -> 评论/预约/借用/维修/使用记录
INSERT INTO equipment
  (id, lab_id, asset_no, name, type, model, status, quantity, location, note, created_at, updated_at)
VALUES
  (1, 1, 'EQ-CH-0001', '离心机', '仪器', 'CEN-200', 'available', 2, '一号楼 A101', NULL, NOW(), NOW()),
  (2, 2, 'EQ-PH-0001', '示波器', '仪器', 'OSC-100', 'available', 6, '二号楼 B203', NULL, NOW(), NOW());

INSERT INTO equipment_comment
  (id, lab_id, equipment_id, user_id, content, status, deleted_by, deleted_at, created_at, updated_at)
VALUES
  (1, 1, 1, 3, '离心机状态良好，操作说明清晰。', 'normal', NULL, NULL, NOW(), NOW());

INSERT INTO lab_review
  (id, lab_id, user_id, rating, content, status, deleted_by, deleted_at, created_at, updated_at)
VALUES
  (1, 1, 3, 5, '环境整洁，安全提示到位，预约流程顺畅。', 'normal', NULL, NULL, NOW(), NOW());

INSERT INTO equipment_reservation
  (id, equipment_id, user_id, reserve_date, start_time, end_time, purpose, status, approver_id, approved_at, created_at, updated_at)
VALUES
  (1, 1, 3, CURDATE(), '14:00:00', '16:00:00', '配合实验计划使用离心机', 'approved', 2, NOW(), NOW(), NOW());

INSERT INTO equipment_borrow
  (id, equipment_id, user_id, borrow_at, due_at, return_at, purpose, status, created_at, updated_at)
VALUES
  (1, 2, 3, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), NULL, '课程实验临时借用', 'borrowing', NOW(), NOW());

INSERT INTO equipment_maintenance
  (id, equipment_id, reporter_id, type, description, status, operator, resolved_at, created_at, updated_at)
VALUES
  (1, 2, 2, 'inspection', '例行检查：接口与探头完好。', 'processing', '设备管理员', NULL, NOW(), NOW());

INSERT INTO equipment_usage_log
  (id, equipment_id, user_id, action, detail, created_at)
VALUES
  (1, 1, 3, 'reserve', '预约使用离心机（与实验计划同步）', NOW()),
  (2, 2, 3, 'borrow',  '借用示波器用于电路测量', NOW());

-- 13) 资源共享：资料 -> 下载日志
INSERT INTO resource_file
  (id, uploader_id, title, description, category, tags, file_url, file_name, file_size, mime_type, download_count, status, created_at, updated_at)
VALUES
  (1, 2, '实验室安全手册', '实验室通用安全规范与应急流程', 'document', '安全,规范', 'https://example.com/safety.pdf', 'safety.pdf', 102400, 'application/pdf', 1, 'active', NOW(), NOW());

INSERT INTO resource_download_log (id, resource_id, user_id, created_at)
VALUES
  (1, 1, 3, NOW());

-- 14) 日志/通知/反馈（依赖 sys_user 的先插 sys_user）
INSERT INTO login_log (id, user_id, username, ip, success, created_at)
VALUES
  (1, 3, 'student', '127.0.0.1', 1, NOW());

INSERT INTO operation_log (id, user_id, action, detail, created_at)
VALUES
  (1, 1, 'init', '初始化系统种子数据', NOW());

INSERT INTO feedback
  (id, user_id, type, title, content, is_anonymous, status, reply, reply_at, created_at)
VALUES
  (1, 3, '建议', '预约页面优化', '希望增加按实验室楼栋筛选功能。', 0, 'pending', NULL, NULL, NOW());

INSERT INTO user_notification
  (id, user_id, type, title, content, read_at, created_at)
VALUES
  (1, 3, 'system', '欢迎使用', '欢迎使用实验室预约系统，祝你实验顺利。', NULL, NOW());

INSERT INTO exception_log (id, type, message, stack, created_at)
VALUES
  (1, 'INFO', 'seed data initialized', NULL, NOW());

-- 15) 角色权限：角色 -> 权限 -> 关联表 -> 用户角色
INSERT INTO sys_role (id, code, name, created_at, updated_at)
VALUES
  (1, 'ADMIN',   '管理员', NOW(), NOW()),
  (2, 'TEACHER', '教师',   NOW(), NOW()),
  (3, 'STUDENT', '学生',   NOW(), NOW());

INSERT INTO sys_permission (id, code, name, created_at, updated_at)
VALUES
  (1, 'LAB:READ',      '查看实验室', NOW(), NOW()),
  (2, 'LAB:MANAGE',    '管理实验室', NOW(), NOW()),
  (3, 'RES:CREATE',    '创建预约',   NOW(), NOW()),
  (4, 'RES:APPROVE',   '审批预约',   NOW(), NOW()),
  (5, 'EQUIP:MANAGE',  '管理设备',   NOW(), NOW()),
  (6, 'RESFILE:UPLOAD','上传资源',   NOW(), NOW());

INSERT INTO sys_role_permission (id, role_id, permission_id, created_at)
VALUES
  (1, 1, 1, NOW()), (2, 1, 2, NOW()), (3, 1, 3, NOW()), (4, 1, 4, NOW()), (5, 1, 5, NOW()), (6, 1, 6, NOW()),
  (7, 2, 1, NOW()), (8, 2, 3, NOW()), (9, 2, 4, NOW()), (10,2, 6, NOW()),
  (11,3, 1, NOW()), (12,3, 3, NOW());

INSERT INTO sys_user_role (id, user_id, role_id, created_at)
VALUES
  (1, 1, 1, NOW()),
  (2, 2, 2, NOW()),
  (3, 3, 3, NOW());

