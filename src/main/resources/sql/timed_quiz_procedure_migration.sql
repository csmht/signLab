-- ============================================
-- 限时答题类型（类型5）数据库变更脚本
-- 创建时间：2025-01-30
-- 说明：为类型5（限时答题）创建独立的配置表
-- ============================================

-- 1. 创建限时答题配置表
-- 该表类似于 procedure_topic 表，但增加了答题时间限制字段
CREATE TABLE IF NOT EXISTS timed_quiz_procedure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    experimental_procedure_id BIGINT NOT NULL COMMENT '实验步骤ID',
    is_random BOOLEAN DEFAULT FALSE COMMENT '是否随机抽取题目',
    topic_number INT COMMENT '题目数量（随机模式有效）',
    topic_tags VARCHAR(500) COMMENT '标签限制（逗号分隔）',
    topic_types VARCHAR(200) COMMENT '题目类型限制（逗号分隔）',
    quiz_time_limit INT NOT NULL COMMENT '答题时间限制（分钟）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_experimental_procedure_id (experimental_procedure_id)
) COMMENT='限时答题题目配置表';

-- 2. 修改 experiment_procedure 表
-- 移除 quiz_time_limit 字段（如果存在），使用 timed_quiz_id 关联 timed_quiz_procedure 表
-- 注意：如果之前已经创建了 quiz_time_limit 字段，需要先删除
ALTER TABLE experiment_procedure
DROP COLUMN IF EXISTS quiz_time_limit;

-- 确保 timed_quiz_id 字段存在（类型5时指向 timed_quiz_procedure 表）
-- 如果字段不存在则添加
ALTER TABLE experiment_procedure
ADD COLUMN IF NOT EXISTS timed_quiz_id BIGINT COMMENT '限时答题配置ID(仅类型5时有效)' AFTER procedure_topic_id;

-- 3. 修改学生答案表，添加锁定字段
ALTER TABLE student_experimental_procedure
ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE COMMENT '是否已锁定(限时答题提交后不可修改)' AFTER is_graded;

-- ============================================
-- 字段说明
-- ============================================
-- timed_quiz_procedure 表字段说明：
--   - id: 主键ID
--   - experimental_procedure_id: 关联的实验步骤ID
--   - is_random: 是否随机抽取题目
--   - topic_number: 题目数量（仅在随机抽取模式下有效）
--   - topic_tags: 标签限制（逗号分隔的标签字符串）
--   - topic_types: 题目类型限制（逗号分隔，如"1,2,3"表示单选、多选、判断）
--   - quiz_time_limit: 答题时间限制（分钟），学生点击"获取题目"后开始计时
--
-- experiment_procedure 表字段说明：
--   - timed_quiz_id: 限时答题配置ID（仅类型5时有效），指向 timed_quiz_procedure 表的ID
--
-- student_experimental_procedure 表字段说明：
--   - is_locked: 是否已锁定（限时答题提交后设置为TRUE，表示不可修改）
--
-- ============================================
-- 使用说明
-- ============================================
-- 1. 教师创建限时答题步骤时：
--    - 在 experiment_procedure 表中创建步骤记录（type=5）
--    - 在 timed_quiz_procedure 表中创建配置记录
--    - 将 timed_quiz_procedure.id 赋值给 experiment_procedure.timed_quiz_id
--
-- 2. 学生查询限时答题步骤详情时：
--    - 后端生成加密密钥（格式：username|timestamp|random_code）
--    - 返回密钥给前端，前端根据密钥中的时间戳计算剩余时间
--
-- 3. 学生提交限时答题答案时：
--    - 前端将密钥和答案一起提交
--    - 后端解密密钥，解析出时间戳
--    - 计算当前时间与密钥时间戳的差值，验证是否超时
--    - 如果超时，拒绝提交；否则保存答案并设置 is_locked=TRUE
--
-- 4. 密钥验证规则：
--    - 密钥格式：username|timestamp|random_code
--    - 使用 CryptoUtil 进行 AES 加密
--    - 时间验证：current_time - key_timestamp <= quiz_time_limit + buffer_time（5分钟）
--    - 密钥不存储在数据库中，仅用于时间验证
