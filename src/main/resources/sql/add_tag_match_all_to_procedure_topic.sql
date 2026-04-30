-- ============================================
-- 课堂小测标签匹配模式字段迁移脚本
-- 对应提交：a40a181487d71c04ae4629351d1fd1f38697fa47
-- 说明：为 procedure_topic 表新增 tag_match_all 字段
-- 含义：0-命中任一标签，1-必须命中全部标签
-- ============================================

-- 1. 新增字段，先允许为空，便于兼容已部署环境中的历史数据
ALTER TABLE `procedure_topic`
ADD COLUMN `tag_match_all` BIT(1) NULL COMMENT '标签匹配方式：0-命中任一标签，1-必须命中全部标签';

-- 2. 回填历史数据
-- 旧逻辑没有该字段，这里统一按“命中全部标签”初始化
UPDATE `procedure_topic`
SET `tag_match_all` = b'1'
WHERE `tag_match_all` IS NULL;

-- 3. 收紧字段约束，改为非空并设置默认值
ALTER TABLE `procedure_topic`
MODIFY COLUMN `tag_match_all` BIT(1) NOT NULL DEFAULT b'1' COMMENT '标签匹配方式：0-命中任一标签，1-必须命中全部标签';
