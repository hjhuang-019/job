-- 用户黑名单：求职者不可投递；企业岗位对求职者不可见且不可新建/上架岗位。登录与消息中心不受影响。
-- 已在库环境可单独执行本脚本；新环境以 schema.sql 为准。

USE job_platform;

ALTER TABLE sys_user
  ADD COLUMN blacklisted TINYINT NOT NULL DEFAULT 0 COMMENT '是否黑名单：0否 1是' AFTER status;

ALTER TABLE sys_user
  ADD COLUMN blacklisted_at DATETIME DEFAULT NULL COMMENT '列入黑名单时间' AFTER blacklisted;

ALTER TABLE sys_user
  ADD COLUMN blacklisted_by BIGINT DEFAULT NULL COMMENT '操作管理员用户ID' AFTER blacklisted_at;

ALTER TABLE sys_user
  ADD COLUMN blacklist_reason VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因' AFTER blacklisted_by;
