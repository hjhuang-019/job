USE job_platform;

SET NAMES utf8mb4;

DELETE FROM favorite_job;
DELETE FROM sys_user_feedback;
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
  expected_city, expected_salary, expected_job, accept_remote, introduction, certificate_path, verify_status, verified_by, verified_at
)
VALUES (
  1, 1, '男', '1998-05-20', '听力障碍', '二级', '本科', 'Java, Spring Boot, Vue3, MySQL',
  '上海', '6k-8k', 'Java后端开发工程师', 1, '有Web项目开发经验，能适应远程协作。', '/uploads/certificates/seeker01-cert.pdf',
  'PENDING', NULL, NULL
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
  '/uploads/licenses/enterprise01-license.jpg', 'PENDING', NULL, NULL
);

INSERT INTO resume (
  id, user_id, resume_name, resume_type, file_path, content_text, education_experience,
  work_experience, project_experience, skill_summary, disability_type, disability_level, is_default, status
)
VALUES
  (
    1, 1, '默认简历-后端方向', 'ONLINE', '/uploads/resumes/seeker01-resume-backend.pdf',
    '2年Java后端开发经验，熟悉就业平台业务与接口联调。',
    '2017-2021 某大学 软件工程专业 本科',
    '2021-2023 某信息技术公司 Java开发工程师',
    '参与就业服务平台、后台管理系统和数据报表系统开发。',
    'Java、Spring Boot、MyBatis、MySQL、Redis、Vue3',
    NULL, NULL,
    1, 1
  ),
  (
    2, 1, '求职简历-全栈方向', 'ONLINE', '/uploads/resumes/seeker01-resume-fullstack.pdf',
    '具备前后端协作开发经验，注重无障碍与可用性优化。',
    '2017-2021 某大学 软件工程专业 本科',
    '2023-至今 某互联网公司 全栈开发工程师',
    '主导招聘平台前后端联调，负责登录、岗位、投递、消息等模块。',
    'Vue3、Element Plus、Pinia、Spring Boot、MySQL',
    NULL, NULL,
    0, 1
  ),
  (
    3, 1, '实习应聘简历-测试方向', 'ONLINE', '/uploads/resumes/seeker01-resume-test.pdf',
    '对接口测试和业务流程验证有经验，可配合交付测试方案。',
    '2017-2021 某大学 软件工程专业 本科',
    '2020-2021 某科技公司 测试实习生',
    '参与中后台系统测试用例设计、缺陷跟踪和回归测试。',
    'Postman、SQL、基础自动化测试',
    NULL, NULL,
    0, 1
  );

INSERT INTO job (
  id, enterprise_user_id, title, category, city, district, salary, work_mode,
  education_requirement, experience_requirement, skill_requirements,
  disability_support_type, accessibility_support_desc, job_description,
  headcount, status, publish_time, expire_time, reviewed_by, reviewed_at
)
VALUES
  (
    1, 2, 'Java后端开发工程师', '软件开发', '上海', '浦东新区', '8k-12k', 'HYBRID',
    '大专及以上', '1-3年', '熟悉Java、Spring Boot、MyBatis、MySQL',
    '听力障碍、肢体障碍', '支持线上会议字幕、弹性工位、无障碍办公环境',
    '负责就业服务平台后端接口开发、联调和维护。',
    2, 'OPEN', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 3, DATE_SUB(NOW(), INTERVAL 6 DAY)
  ),
  (
    2, 2, '前端开发工程师(Vue3)', '前端开发', '上海', '徐汇区', '7k-10k', 'REMOTE',
    '大专及以上', '1年以上', '熟悉Vue3、Element Plus、Axios',
    '肢体障碍、视力障碍', '支持远程协作、键盘导航、高对比度主题',
    '负责招聘平台前端页面开发与无障碍优化。',
    1, 'OPEN', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), 3, DATE_SUB(NOW(), INTERVAL 4 DAY)
  ),
  (
    3, 2, '测试工程师', '测试', '上海', '闵行区', '6k-9k', 'OFFLINE',
    '大专及以上', '1年以上', '掌握接口测试、SQL、缺陷管理流程',
    '听力障碍', '提供文本沟通和无障碍办公动线',
    '负责功能测试、接口测试、回归测试与上线验收。',
    1, 'OPEN', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 3, DATE_SUB(NOW(), INTERVAL 2 DAY)
  ),
  (
    4, 2, '产品助理', '产品', '杭州', '西湖区', '6k-8k', 'HYBRID',
    '本科', '应届可投', '具备文档撰写能力和需求分析能力',
    '肢体障碍', '支持混合办公及弹性工作时间',
    '协助产品经理完成需求整理、竞品调研和原型输出。',
    1, 'CLOSED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 3, DATE_SUB(NOW(), INTERVAL 15 DAY)
  ),
  (
    5, 2, '运维工程师', '运维', '南京', '鼓楼区', '9k-13k', 'OFFLINE',
    '本科', '2年以上', '熟悉Linux、Nginx、Docker、监控告警',
    '听力障碍', '支持文本告警和远程值班协作',
    '负责应用部署、监控维护、故障应急与性能优化。',
    1, 'DRAFT', NULL, NULL, NULL, NULL
  );

INSERT INTO job_application (
  id, job_id, job_seeker_user_id, resume_id, status, apply_time, feedback_content, handled_time
)
VALUES
  (1, 1, 1, 1, 'VIEWED', DATE_SUB(NOW(), INTERVAL 4 DAY), '简历已查看，后续将安排电话沟通。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (2, 2, 1, 2, 'COMMUNICATING', DATE_SUB(NOW(), INTERVAL 3 DAY), '已进入沟通阶段，请保持手机畅通。', DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (3, 3, 1, 3, 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 DAY), '当前岗位更偏向线下高强度测试，后续有合适岗位再联系。', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO sys_message (
  id, receiver_user_id, sender_user_id, message_type, title, content,
  related_business_type, related_business_id, is_read, read_time
)
VALUES
  (1, 1, NULL, 'SYSTEM', '欢迎使用平台', '欢迎注册残疾人就业Web平台，建议先完善资料并上传认证材料。', 'USER', 1, 0, NULL),
  (2, 1, 2, 'APPLY_STATUS', '投递状态更新', '你投递的岗位《Java后端开发工程师》状态已更新为：已查看。', 'JOB_APPLICATION', 1, 0, NULL),
  (3, 1, 2, 'APPLY_STATUS', '投递状态更新', '你投递的岗位《前端开发工程师(Vue3)》状态已更新为：沟通中。', 'JOB_APPLICATION', 2, 0, NULL),
  (4, 1, 2, 'APPLY_STATUS', '投递状态更新', '你投递的岗位《测试工程师》状态已更新为：已拒绝。备注：当前岗位更偏向线下高强度测试。', 'JOB_APPLICATION', 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (5, 1, 3, 'AUDIT_NOTICE', '求职者认证审核结果', '你的求职者认证当前处于待审核状态，管理员将在1个工作日内处理。', 'JOB_SEEKER_PROFILE', 1, 0, NULL),
  (6, 2, 3, 'AUDIT_NOTICE', '企业认证审核结果', '你的企业认证当前处于待审核状态，请留意后续审核消息。', 'ENTERPRISE_PROFILE', 1, 0, NULL);

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
  (1, 'JOB_SEEKER_PROFILE', 1, 'PENDING', '求职者提交认证，等待管理员审核。', 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (2, 'ENTERPRISE_PROFILE', 1, 'PENDING', '企业提交认证，等待管理员审核。', 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (3, 'JOB', 1, 'APPROVED', '岗位信息完整，允许发布。', 3, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (4, 'JOB', 2, 'APPROVED', '岗位信息完整，允许发布。', 3, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (5, 'JOB', 3, 'APPROVED', '岗位信息完整，允许发布。', 3, DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO favorite_job (id, job_id, job_seeker_user_id, created_at)
VALUES
  (1, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (2, 2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));
