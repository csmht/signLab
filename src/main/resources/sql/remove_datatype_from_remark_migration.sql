-- ============================================================
-- 数据迁移：规范化 data_collection.remark JSON 格式
-- 执行时间：待定
-- 说明：
--   1. 早期旧格式（如 id=1,2,3）：{"dataFields":["Uab","I1"]}
--      → 迁移为 {"fillBlanks":[{"fieldName":"Uab","value":""},{"fieldName":"I1","value":""}]}
--      （correct_answer 列已存储正确答案，remark 中仅保留字段名）
--
--   2. 含冗余字段的新格式（如 id=7,9）：
--      {"fillBlanks":{"Uab":"220"},"dataType":1,"correctAnswer":{"Uab":"220"},"tolerance":5}
--      → 移除 dataType、correctAnswer、tolerance 冗余字段
--      → fillBlanks 从 Map 转为 List 格式：
--      {"fillBlanks":[{"fieldName":"Uab","value":"220","tolerance":5}]}
--
--   3. 表格类型新格式（含 tableCellAnswers 为 Map + cellTolerances）：
--      → tableCellAnswers 从 Map 合并为 List<TableCellAnswer> 格式
--      → columnTolerances 从 Map 转为 List<ColumnTolerance> 格式
--
--   4. 空记录（如 id=4,5,6,8）：type=3 文件上传，remark='{}'
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
-- 第三部分：迁移步骤一 — 旧格式 dataFields → fillBlanks (List 格式)
-- ============================================================
-- 将 {"dataFields":["Uab","I1","Us"]} 转为 {"fillBlanks":[{"fieldName":"Uab","value":""},{"fieldName":"I1","value":""},{"fieldName":"Us","value":""}]}
-- 正确答案已存在于 correct_answer 列，remark 中仅保留字段名

UPDATE data_collection
SET remark = JSON_OBJECT(
    'fillBlanks',
    (
        SELECT JSON_ARRAYAGG(JSON_OBJECT('fieldName', field, 'value', ''))
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
-- 第五部分：迁移步骤三 — fillBlanks 从 Map 格式转为 List 格式
-- ============================================================
-- 将 {"fillBlanks":{"Uab":"220","I1":"5"}} 转为
--     {"fillBlanks":[{"fieldName":"Uab","value":"220"},{"fieldName":"I1","value":"5"}]}
-- 此步骤处理那些已经是 Map 格式但需要转为 List 格式的记录

UPDATE data_collection dc
SET dc.remark = JSON_OBJECT(
    'fillBlanks',
    (
        SELECT JSON_ARRAYAGG(JSON_OBJECT('fieldName', jt.k, 'value', jt.v))
        FROM JSON_TABLE(
            JSON_EXTRACT(dc.remark, '$.fillBlanks'),
            '$[*]' COLUMNS (
                k VARCHAR(255) PATH '$.key',
                v VARCHAR(255) PATH '$.value'
            )
        ) AS jt
    )
)
WHERE dc.remark IS NOT NULL
  AND dc.remark != ''
  AND dc.type = 1
  AND JSON_CONTAINS_PATH(dc.remark, 'one', '$.fillBlanks')
  AND JSON_TYPE(JSON_EXTRACT(dc.remark, '$.fillBlanks')) = 'OBJECT';

-- 验证步骤三：检查填空类型是否还有 Map 格式的 fillBlanks
SELECT id, type, JSON_EXTRACT(remark, '$.fillBlanks') AS fillBlanks
FROM data_collection
WHERE type = 1
  AND remark IS NOT NULL
  AND remark != ''
  AND JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks')
  AND JSON_TYPE(JSON_EXTRACT(remark, '$.fillBlanks')) = 'OBJECT';

-- 预期结果：0 行

-- ============================================================
-- 第六部分：迁移步骤四 — tableCellAnswers 从 Map 合并为 List，columnTolerances 从 Map 转为 List
-- ============================================================
-- 将 {"tableCellAnswers":{"A1":"3.5","B1":"4.2"},"cellTolerances":{"A1":5.0},"columnTolerances":{"A":3.0}} 转为
--     {"tableCellAnswers":[{"cellPosition":"A1","value":"3.5","tolerance":5.0},{"cellPosition":"B1","value":"4.2"}],
--      "columnTolerances":[{"columnName":"A","tolerance":3.0}]}

-- 先处理 tableCellAnswers：Map → List（合并 cellTolerances）
UPDATE data_collection dc
SET dc.remark = JSON_SET(
    dc.remark,
    '$.tableCellAnswers',
    (
        SELECT JSON_ARRAYAGG(
            JSON_OBJECT(
                'cellPosition', jt.cell_pos,
                'value', jt.cell_val,
                'tolerance', JSON_UNQUOTE(JSON_EXTRACT(dc.remark, CONCAT('$.cellTolerances.', jt.cell_pos)))
            )
        )
        FROM JSON_TABLE(
            JSON_EXTRACT(dc.remark, '$.tableCellAnswers'),
            '$[*]' COLUMNS (
                cell_pos VARCHAR(255) PATH '$.key',
                cell_val VARCHAR(255) PATH '$.value'
            )
        ) AS jt
    )
)
WHERE dc.remark IS NOT NULL
  AND dc.remark != ''
  AND dc.type = 2
  AND JSON_CONTAINS_PATH(dc.remark, 'one', '$.tableCellAnswers')
  AND JSON_TYPE(JSON_EXTRACT(dc.remark, '$.tableCellAnswers')) = 'OBJECT';

-- 移除已合并的 cellTolerances 字段
UPDATE data_collection
SET remark = JSON_REMOVE(remark, '$.cellTolerances')
WHERE remark IS NOT NULL
  AND remark != ''
  AND type = 2
  AND JSON_CONTAINS_PATH(remark, 'one', '$.cellTolerances');

-- 处理 columnTolerances：Map → List
UPDATE data_collection dc
SET dc.remark = JSON_SET(
    dc.remark,
    '$.columnTolerances',
    (
        SELECT JSON_ARRAYAGG(JSON_OBJECT('columnName', jt.col_name, 'tolerance', jt.col_tol))
        FROM JSON_TABLE(
            JSON_EXTRACT(dc.remark, '$.columnTolerances'),
            '$[*]' COLUMNS (
                col_name VARCHAR(255) PATH '$.key',
                col_tol DECIMAL(10,2) PATH '$.value'
            )
        ) AS jt
    )
)
WHERE dc.remark IS NOT NULL
  AND dc.remark != ''
  AND dc.type = 2
  AND JSON_CONTAINS_PATH(dc.remark, 'one', '$.columnTolerances')
  AND JSON_TYPE(JSON_EXTRACT(dc.remark, '$.columnTolerances')) = 'OBJECT';

-- 验证步骤四
SELECT id, type,
       JSON_EXTRACT(remark, '$.tableCellAnswers') AS tableCellAnswers,
       JSON_EXTRACT(remark, '$.columnTolerances') AS columnTolerances
FROM data_collection
WHERE type = 2
  AND remark IS NOT NULL
  AND remark != '';

-- ============================================================
-- 第七部分：迁移后最终验证
-- ============================================================

-- 1. 查看所有迁移后的 remark 数据
SELECT id, type, remark
FROM data_collection
WHERE remark IS NOT NULL AND remark != ''
ORDER BY id;

-- 2. 确认所有填空类型记录都使用 fillBlanks (List 格式)
SELECT id, type, remark
FROM data_collection
WHERE type = 1
  AND remark IS NOT NULL
  AND remark != ''
  AND NOT JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks');

-- 预期结果：0 行

-- 3. 确认不再有 Map 格式的 fillBlanks
SELECT id, type, remark
FROM data_collection
WHERE type = 1
  AND remark IS NOT NULL
  AND remark != ''
  AND JSON_CONTAINS_PATH(remark, 'one', '$.fillBlanks')
  AND JSON_TYPE(JSON_EXTRACT(remark, '$.fillBlanks')) = 'OBJECT';

-- 预期结果：0 行

-- ============================================================
-- 第八部分：回滚方案（如需回退）
-- ============================================================

-- UPDATE data_collection dc
-- INNER JOIN data_collection_remark_backup_20260415 bak ON dc.id = bak.id
-- SET dc.remark = bak.remark;

-- ============================================================
-- 第九部分：清理（确认迁移成功后执行）
-- ============================================================

-- DROP TABLE IF EXISTS data_collection_remark_backup_20260415;
