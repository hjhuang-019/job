CREATE DATABASE IF NOT EXISTS job_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE job_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS favorite_job;
DROP TABLE IF EXISTS sys_audit_log;
DROP TABLE IF EXISTS sys_announcement;
DROP TABLE IF EXISTS sys_message;
DROP TABLE IF EXISTS job_application;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS enterprise_profile;
DROP TABLE IF EXISTS job_seeker_profile;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  role VARCHAR(20) NOT NULL COMMENT '角色：JOB_SEEKER/ENTERPRISE/ADMIN',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1正常',
  real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_role (role),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE job_seeker_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '求职者资料ID',
  user_id BIGINT NOT NULL COMMENT '关联用户ID',
  gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
  birth_date DATE DEFAULT NULL COMMENT '出生日期',
  disability_type VARCHAR(100) DEFAULT NULL COMMENT '残疾类别',
  disability_level VARCHAR(20) DEFAULT NULL COMMENT '残疾等级',
  education VARCHAR(50) DEFAULT NULL COMMENT '学历',
  skills TEXT COMMENT '技能描述',
  expected_city VARCHAR(100) DEFAULT NULL COMMENT '期望城市',
  expected_salary VARCHAR(50) DEFAULT NULL COMMENT '期望薪资',
  introduction VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  certificate_path VARCHAR(255) DEFAULT NULL COMMENT '残疾证或相关证明路径',
  verify_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  verified_by BIGINT DEFAULT NULL COMMENT '审核人用户ID',
  verified_at DATETIME DEFAULT NULL COMMENT '审核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_job_seeker_profile_user_id (user_id),
  KEY idx_job_seeker_verify_status (verify_status),
  CONSTRAINT fk_job_seeker_profile_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求职者资料表';

CREATE TABLE enterprise_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '企业资料ID',
  user_id BIGINT NOT NULL COMMENT '关联用户ID',
  enterprise_name VARCHAR(200) NOT NULL COMMENT '企业名称',
  unified_social_credit_code VARCHAR(50) DEFAULT NULL COMMENT '统一社会信用代码',
  industry VARCHAR(100) DEFAULT NULL COMMENT '所属行业',
  scale_type VARCHAR(50) DEFAULT NULL COMMENT '企业规模',
  contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  address VARCHAR(255) DEFAULT NULL COMMENT '企业地址',
  description VARCHAR(1000) DEFAULT NULL COMMENT '企业简介',
  accessibility_support_desc VARCHAR(500) DEFAULT NULL COMMENT '无障碍支持说明',
  license_path VARCHAR(255) DEFAULT NULL COMMENT '营业执照路径',
  verify_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  verified_by BIGINT DEFAULT NULL COMMENT '审核人用户ID',
  verified_at DATETIME DEFAULT NULL COMMENT '审核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_enterprise_profile_user_id (user_id),
  KEY idx_enterprise_verify_status (verify_status),
  KEY idx_enterprise_name (enterprise_name),
  CONSTRAINT fk_enterprise_profile_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业资料表';

CREATE TABLE resume (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
  user_id BIGINT NOT NULL COMMENT '求职者用户ID',
  resume_name VARCHAR(100) NOT NULL COMMENT '简历名称',
  resume_type VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT '简历类型：ONLINE/FILE',
  file_path VARCHAR(255) DEFAULT NULL COMMENT '附件简历路径',
  content_text TEXT COMMENT '在线简历内容',
  education_experience TEXT COMMENT '教育经历',
  work_experience TEXT COMMENT '工作经历',
  project_experience TEXT COMMENT '项目经历',
  skill_summary TEXT COMMENT '技能总结',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认简历：0否，1是',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用，1正常',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_resume_user_id (user_id),
  KEY idx_resume_is_default (is_default),
  CONSTRAINT fk_resume_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

CREATE TABLE job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
  enterprise_user_id BIGINT NOT NULL COMMENT '企业用户ID',
  title VARCHAR(100) NOT NULL COMMENT '岗位名称',
  category VARCHAR(100) DEFAULT NULL COMMENT '岗位分类',
  city VARCHAR(100) NOT NULL COMMENT '工作城市',
  district VARCHAR(100) DEFAULT NULL COMMENT '区县',
  salary VARCHAR(50) DEFAULT NULL COMMENT '薪资范围',
  work_mode VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '工作方式：OFFLINE/REMOTE/HYBRID',
  education_requirement VARCHAR(50) DEFAULT NULL COMMENT '学历要求',
  experience_requirement VARCHAR(50) DEFAULT NULL COMMENT '经验要求',
  skill_requirements TEXT COMMENT '技能要求',
  disability_support_type VARCHAR(255) DEFAULT NULL COMMENT '适配残障类型说明',
  accessibility_support_desc VARCHAR(500) DEFAULT NULL COMMENT '岗位无障碍支持说明',
  job_description TEXT COMMENT '岗位描述',
  headcount INT NOT NULL DEFAULT 1 COMMENT '招聘人数',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PENDING/OPEN/CLOSED/REJECTED',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  expire_time DATETIME DEFAULT NULL COMMENT '截止时间',
  reviewed_by BIGINT DEFAULT NULL COMMENT '审核人用户ID',
  reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_job_enterprise_user_id (enterprise_user_id),
  KEY idx_job_city (city),
  KEY idx_job_status (status),
  KEY idx_job_publish_time (publish_time),
  CONSTRAINT fk_job_enterprise_user_id FOREIGN KEY (enterprise_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位表';

CREATE TABLE job_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投递记录ID',
  job_id BIGINT NOT NULL COMMENT '岗位ID',
  job_seeker_user_id BIGINT NOT NULL COMMENT '求职者用户ID',
  resume_id BIGINT NOT NULL COMMENT '简历ID',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '投递状态：PENDING/VIEWED/INTERVIEW/PASSED/REJECTED/WITHDRAWN',
  apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  feedback_content VARCHAR(500) DEFAULT NULL COMMENT '企业反馈内容',
  handled_time DATETIME DEFAULT NULL COMMENT '处理时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_job_application_unique (job_id, job_seeker_user_id),
  KEY idx_job_application_status (status),
  KEY idx_job_application_apply_time (apply_time),
  CONSTRAINT fk_job_application_job_id FOREIGN KEY (job_id) REFERENCES job (id),
  CONSTRAINT fk_job_application_job_seeker_user_id FOREIGN KEY (job_seeker_user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_job_application_resume_id FOREIGN KEY (resume_id) REFERENCES resume (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位投递记录表';

CREATE TABLE sys_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  receiver_user_id BIGINT NOT NULL COMMENT '接收人用户ID',
  sender_user_id BIGINT DEFAULT NULL COMMENT '发送人用户ID，系统消息可为空',
  message_type VARCHAR(20) NOT NULL COMMENT '消息类型：SYSTEM/ENTERPRISE/APPLICATION/AUDIT',
  title VARCHAR(100) NOT NULL COMMENT '消息标题',
  content VARCHAR(1000) NOT NULL COMMENT '消息内容',
  related_business_type VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',
  related_business_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
  is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
  read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_sys_message_receiver_user_id (receiver_user_id),
  KEY idx_sys_message_message_type (message_type),
  KEY idx_sys_message_is_read (is_read),
  CONSTRAINT fk_sys_message_receiver_user_id FOREIGN KEY (receiver_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统消息表';

CREATE TABLE sys_announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
  title VARCHAR(150) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/OFFLINE',
  is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0否，1是',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  created_by BIGINT NOT NULL COMMENT '创建人用户ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_sys_announcement_status (status),
  KEY idx_sys_announcement_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

CREATE TABLE sys_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核日志ID',
  business_type VARCHAR(30) NOT NULL COMMENT '业务类型：JOB_SEEKER_PROFILE/ENTERPRISE_PROFILE/JOB',
  business_id BIGINT NOT NULL COMMENT '业务ID',
  audit_status VARCHAR(20) NOT NULL COMMENT '审核结果：PENDING/APPROVED/REJECTED',
  audit_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  operator_user_id BIGINT NOT NULL COMMENT '审核人用户ID',
  operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_sys_audit_log_business (business_type, business_id),
  KEY idx_sys_audit_log_operator_user_id (operator_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志表';

CREATE TABLE favorite_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏记录ID',
  job_id BIGINT NOT NULL COMMENT '岗位ID',
  job_seeker_user_id BIGINT NOT NULL COMMENT '求职者用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_favorite_job_unique (job_id, job_seeker_user_id),
  KEY idx_favorite_job_user_id (job_seeker_user_id),
  CONSTRAINT fk_favorite_job_job_id FOREIGN KEY (job_id) REFERENCES job (id),
  CONSTRAINT fk_favorite_job_user_id FOREIGN KEY (job_seeker_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位收藏表';

SET FOREIGN_KEY_CHECKS = 1;
