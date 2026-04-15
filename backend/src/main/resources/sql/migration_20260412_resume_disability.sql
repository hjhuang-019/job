-- 简历：残疾类型（多选存逗号分隔）、残疾等级
ALTER TABLE resume
  ADD COLUMN disability_type VARCHAR(200) DEFAULT NULL COMMENT '残疾类别（多选逗号分隔）' AFTER skill_summary,
  ADD COLUMN disability_level VARCHAR(20) DEFAULT NULL COMMENT '残疾等级' AFTER disability_type;
