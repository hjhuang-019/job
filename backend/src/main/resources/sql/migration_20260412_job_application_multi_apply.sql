-- 允许同一求职者对同一岗位多次投递；保留非唯一索引便于按岗位+求职者查询
ALTER TABLE job_application DROP INDEX uk_job_application_unique;
ALTER TABLE job_application ADD INDEX idx_job_application_job_seeker (job_id, job_seeker_user_id);
