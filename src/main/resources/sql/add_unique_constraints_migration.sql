-- 数据库迁移：为 selectOne 依赖的高风险表补充唯一约束
-- 执行时间：2026-04-04
-- 说明：
-- 1. 只为业务上明确应该唯一的数据加约束
-- 2. classes_experiment / classroom_quiz 仍需业务层保证，不能用简单唯一索引硬兜
-- 1. users 表：微信 OpenID 应唯一
-- 执行前请先排查重复数据：
-- SELECT wx_openid, COUNT(*) FROM users WHERE wx_openid IS NOT NULL AND wx_openid <> '' GROUP BY wx_openid HAVING COUNT(*) > 1;
ALTER TABLE `users`
ADD UNIQUE KEY `uk_wx_openid` (`wx_openid`);

-- 2. classes 表：补充验证码字段并保证唯一
ALTER TABLE `classes`
ADD COLUMN `verification_code` VARCHAR(20) NULL COMMENT '班级验证码' AFTER `class_name`;

-- 为已有班级回填验证码，使用主键保证唯一
UPDATE `classes`
SET `verification_code` = CONCAT('CLS', `id`)
WHERE `verification_code` IS NULL OR `verification_code` = '';

ALTER TABLE `classes`
MODIFY COLUMN `verification_code` VARCHAR(20) NOT NULL COMMENT '班级验证码',
ADD UNIQUE KEY `uk_verification_code` (`verification_code`);

-- 3. student_experimental_procedure 表：同一学生对同一步骤只能有一条记录
-- 执行前请先排查重复数据：
-- SELECT experimental_procedure_id, student_username, COUNT(*) FROM student_experimental_procedure GROUP BY experimental_procedure_id, student_username HAVING COUNT(*) > 1;
ALTER TABLE `student_experimental_procedure`
ADD UNIQUE KEY `uk_procedure_student` (`experimental_procedure_id`, `student_username`);

-- 4. data_collection 表：一个实验步骤只能对应一条数据收集配置
-- 执行前请先排查重复数据：
-- SELECT experimental_procedure_id, COUNT(*) FROM data_collection GROUP BY experimental_procedure_id HAVING COUNT(*) > 1;
ALTER TABLE `data_collection`
ADD UNIQUE KEY `uk_procedure_data` (`experimental_procedure_id`);

-- 5. 以下两类风险不通过数据库唯一约束处理：
-- classes_experiment：同一课程/实验可以在不同时间、不同班级组合下重复开课
-- classroom_quiz：同一班级实验允许有多次历史小测，只要求“进行中”最多一个
