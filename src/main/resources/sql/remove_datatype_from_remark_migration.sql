-- ============================================================
-- 数据迁移：规范化 data_collection.remark JSON 格式
-- 执行时间：待定
-- 说明：
--   1. 早期旧格式（id=1,2,3）：{"dataFields":["Uab","I1"]}
--      → 迁移为 {"fillBlanks":{"Uab":"","I1":""}}
--      （correct_answer 列已存储正确答案，remark 中仅保留字段名）
--
--   2. 含冗余字段的新格式（id=7,9）：
--      {"fillBlanks":...,"dataType":1,"correctAnswer":...,"tolerance":...}
--      → 移除 dataType、correctAnswer、tolerance 冗余字段
--
--   3. 空记录（id=4,5,6,8）：type=3 文件上传，remark='{}'
--      → 无需处理
-- ============================================================

-- ============================================================
-- 第一部分：迁移前验证
-- ============================================================

-- 1. 查看所有 remark 数据
SELECT id, type, remark
FROM data_collection
WHERE remark IS NOT NULL AND remark != ''
ORDER BY id;

-- 2. 统计旧格式记录数（含 dataFields 但不含 fillBlanks）
SELECT '旧格式(dataFields)记录数' AS info, COUNT(*) AS total
FROM data_collection
WHERE remark IS NOT NULL
  AND remark != ''
  AND JSON_CONTAINS_PATH(remark, 'one', '$.dataFields')
  AND NOT JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks');

-- 3. 统计含冗余字段的记录数（含 dataType 或 correctAnswer 或 tolerance）
SELECT '含冗余字段记录数' AS info, COUNT(*) AS total
FROM data_collection
WHERE remark IS NOT NULL
  AND remark != ''
  AND (
    JSON_CONTAINS_PATH(remark, 'one', '$.dataType')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.correctAnswer')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.tolerance')
  );

-- ============================================================
-- 第二部分：备份数据（强烈建议）
-- ============================================================

CREATE TABLE IF NOT EXISTS data_collection_remark_backup_20260415 AS
SELECT id, type, remark
FROM data_collection
WHERE remark IS NOT NULL AND remark != '';

SELECT '备份行数' AS info, COUNT(*) AS total
FROM data_collection_remark_backup_20260415;

-- ============================================================
-- 第三部分：迁移步骤一 — 旧格式 dataFields → fillBlanks
-- ============================================================
-- 将 {"dataFields":["Uab","I1","Us"]} 转为 {"fillBlanks":{"Uab":"","I1":"","Us":""}}
-- 正确答案已存在于 correct_answer 列，remark 仅需保留字段名映射

UPDATE data_collection
SET remark = JSON_OBJECT(
    'fillBlanks',
    (
        SELECT JSON_OBJECTAGG(field, '')
        FROM JSON_TABLE(
            JSON_EXTRACT(remark, '$.dataFields'),
            '$[*]' COLUMNS (field VARCHAR(255) PATH '$')
        ) AS jt
    )
)
WHERE remark IS NOT NULL
  AND remark != ''
  AND JSON_CONTAINS_PATH(remark, 'one', '$.dataFields')
  AND NOT JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks');

-- 验证步骤一
SELECT '步骤一迁移后：旧格式剩余' AS info, COUNT(*) AS total
FROM data_collection
WHERE remark IS NOT NULL
  AND remark != ''
  AND JSON_CONTAINS_PATH(remark, 'one', '$.dataFields')
  AND NOT JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks');

-- ============================================================
-- 第四部分：迁移步骤二 — 移除冗余字段 dataType/correctAnswer/tolerance
-- ============================================================

UPDATE data_collection
SET remark = JSON_REMOVE(
    JSON_REMOVE(
        JSON_REMOVE(remark, '$.dataType'),
        '$.correctAnswer'
    ),
    '$.tolerance'
)
WHERE remark IS NOT NULL
  AND remark != ''
  AND (
    JSON_CONTAINS_PATH(remark, 'one', '$.dataType')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.correctAnswer')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.tolerance')
  );

-- 验证步骤二
SELECT '步骤二迁移后：仍含冗余字段的记录数' AS info, COUNT(*) AS total
FROM data_collection
WHERE remark IS NOT NULL
  AND remark != ''
  AND (
    JSON_CONTAINS_PATH(remark, 'one', '$.dataType')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.correctAnswer')
    OR JSON_CONTAINS_PATH(remark, 'one', '$.tolerance')
  );

-- ============================================================
-- 第五部分：迁移后最终验证
-- ============================================================

-- 1. 查看所有迁移后的 remark 数据
SELECT id, type, remark
FROM data_collection
WHERE remark IS NOT NULL AND remark != ''
ORDER BY id;

-- 2. 确认所有填空类型记录都使用 fillBlanks 格式
SELECT id, type, remark
FROM data_collection
WHERE type = 1
  AND remark IS NOT NULL
  AND remark != ''
  AND NOT JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks');

-- 预期结果：0 行（所有填空类型都已包含 fillBlanks）

-- ============================================================
-- 第六部分：回滚方案（如需回退）
-- ============================================================

-- UPDATE data_collection dc
-- INNER JOIN data_collection_remark_backup_20260415 bak ON dc.id = bak.id
-- SET dc.remark = bak.remark;

-- ============================================================
-- 第七部分：清理（确认迁移成功后执行）
-- ============================================================

-- DROP TABLE IF EXISTS data_collection_remark_backup_20260415;
