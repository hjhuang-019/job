USE job_platform;

SET NAMES utf8mb4;

DELETE FROM favorite_job;
DELETE FROM sys_audit_log;
DELETE FROM sys_announcement;
DELETE FROM sys_message;
DELETE FROM job_application;
DELETE FROM job;
DELETE FROM resume;
DELETE FROM enterprise_profile;
DELETE FROM job_seeker_profile;
DELETE FROM sys_user;

INSERT INTO sys_user (id, username, password, role, status, real_name, phone, email, last_login_time)
VALUES
  (1, 'seeker01', '$2a$10$AVbujneNOOrZDavTYgR89u/BtZcJFQfja.32XGDyuGmqxRcfh/Ga6', 'JOB_SEEKER', 1, '张三', '13800000001', 'seeker01@example.com', NOW()),
  (2, 'enterprise01', '$2a$10$AVbujneNOOrZDavTYgR89u/BtZcJFQfja.32XGDyuGmqxRcfh/Ga6', 'ENTERPRISE', 1, '李经理', '13800000002', 'enterprise01@example.com', NOW()),
  (3, 'admin01', '$2a$10$AVbujneNOOrZDavTYgR89u/BtZcJFQfja.32XGDyuGmqxRcfh/Ga6', 'ADMIN', 1, '管理员', '13800000003', 'admin01@example.com', NOW());

INSERT INTO job_seeker_profile (
  id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
  expected_city, expected_salary, introduction, certificate_path, verify_status, verified_by, verified_at
)
VALUES (
  1, 1, '男', '1998-05-20', '听力障碍', '二级', '本科', 'Java, Spring Boot, Vue3, MySQL',
  '上海', '6k-8k', '有Web项目开发经验，能适应远程协作。', '/uploads/certificates/seeker01-cert.pdf',
  'APPROVED', 3, NOW()
);

INSERT INTO enterprise_profile (
  id, user_id, enterprise_name, unified_social_credit_code, industry, scale_type,
  contact_person, contact_phone, address, description, accessibility_support_desc,
  license_path, verify_status, verified_by, verified_at
)
VALUES (
  1, 2, '星光科技有限公司', '91310000MA12345678', '互联网', '100-499人',
  '李经理', '021-88886666', '上海市浦东新区示例路88号',
  '专注于企业数字化服务和政务信息化产品开发。', '支持远程办公、无障碍沟通工具、弹性工时。',
  '/uploads/licenses/enterprise01-license.jpg', 'APPROVED', 3, NOW()
);

INSERT INTO resume (
  id, user_id, resume_name, resume_type, file_path, content_text, education_experience,
  work_experience, project_experience, skill_summary, is_default, status
)
VALUES (
  1, 1, '默认简历', 'ONLINE', '/uploads/resumes/seeker01-resume.pdf',
  '具备Java后端开发与前端基础能力，可参与中小型Web项目开发。',
  '2017-2021 某大学 软件工程专业 本科',
  '2021-2023 某信息技术公司 Java开发工程师',
  '参与就业服务平台、后台管理系统和数据报表系统开发。',
  'Java、Spring Boot、MyBatis、Vue3、MySQL',
  1, 1
);

INSERT INTO job (
  id, enterprise_user_id, title, category, city, district, salary, work_mode,
  education_requirement, experience_requirement, skill_requirements,
  disability_support_type, accessibility_support_desc, job_description,
  headcount, status, publish_time, expire_time, reviewed_by, reviewed_at
)
VALUES (
  1, 2, 'Java后端开发工程师', '软件开发', '上海', '浦东新区', '8k-12k', 'HYBRID',
  '大专及以上', '1-3年', '熟悉Java、Spring Boot、MyBatis、MySQL',
  '听力障碍、肢体障碍', '支持线上会议字幕、弹性工位、无障碍办公环境',
  '负责就业服务平台后端接口开发、联调和维护。',
  2, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 3, NOW()
);

INSERT INTO job_application (
  id, job_id, job_seeker_user_id, resume_id, status, apply_time, feedback_content, handled_time
)
VALUES (
  1, 1, 1, 1, 'VIEWED', NOW(), '简历已查看，后续将安排电话沟通。', NOW()
);

INSERT INTO sys_message (
  id, receiver_user_id, sender_user_id, message_type, title, content,
  related_business_type, related_business_id, is_read, read_time
)
VALUES
  (1, 1, NULL, 'SYSTEM', '欢迎使用平台', '欢迎注册残疾人就业Web平台，祝你求职顺利。', 'USER', 1, 0, NULL),
  (2, 1, 2, 'ENTERPRISE', '岗位投递进展', '你投递的Java后端开发工程师岗位已被企业查看。', 'JOB_APPLICATION', 1, 0, NULL),
  (3, 2, 3, 'AUDIT', '企业资料审核通过', '你的企业资料已审核通过，可开始发布岗位。', 'ENTERPRISE_PROFILE', 1, 1, NOW());

INSERT INTO sys_announcement (
  id, title, content, status, is_top, publish_time, created_by, updated_by
)
VALUES (
  1, '平台试运行公告',
  '残疾人就业Web平台第一版已上线试运行，欢迎求职者完善资料、企业发布岗位。',
  'PUBLISHED', 1, NOW(), 3, 3
);

INSERT INTO sys_audit_log (
  id, business_type, business_id, audit_status, audit_comment, operator_user_id, operated_at
)
VALUES
  (1, 'JOB_SEEKER_PROFILE', 1, 'APPROVED', '资料完整，审核通过。', 3, NOW()),
  (2, 'ENTERPRISE_PROFILE', 1, 'APPROVED', '营业执照清晰，审核通过。', 3, NOW()),
  (3, 'JOB', 1, 'APPROVED', '岗位信息完整，允许发布。', 3, NOW());

INSERT INTO favorite_job (id, job_id, job_seeker_user_id, created_at)
VALUES (1, 1, 1, NOW());
