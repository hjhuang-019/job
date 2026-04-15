-- 投递记录：企业回复可附带面试时间、地址、HR 联系方式（均为可空）
ALTER TABLE job_application
  ADD COLUMN interview_time DATETIME DEFAULT NULL COMMENT '面试时间' AFTER feedback_content,
  ADD COLUMN interview_address VARCHAR(300) DEFAULT NULL COMMENT '面试地址' AFTER interview_time,
  ADD COLUMN hr_contact VARCHAR(120) DEFAULT NULL COMMENT 'HR联系方式' AFTER interview_address;
