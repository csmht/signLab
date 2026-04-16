# 表格类型数据收集格式约定书

> 本文档规定表格类型（type=2）数据收集步骤在前后端交互、数据库存储、自动判分中的数据格式标准。所有参与表格类型数据收集功能的开发人员必须遵守此约定。

---

## 一、基本概念

表格类型数据收集用于需要多行多列结构化数据填写的实验步骤。表格由**行表头**和**列表头**定义结构，单元格通过 **`rowIndex`（行索引）+ `columnIndex`（列索引）** 两个整型字段定位。

### 索引规则

- `rowIndex` 和 `columnIndex` **从 0 开始**
- `rowIndex` 对应 `tableRowHeaders` 数组下标
- `columnIndex` 对应 `tableColumnHeaders` 数组下标

### 示例表格

| | 温度（columnIndex=0） | 湿度（columnIndex=1） | 压力（columnIndex=2） |
|---|---|---|---|
| **实验1（rowIndex=0）** | 3.5 | 60 | 101.3 |
| **实验2（rowIndex=1）** | 4.2 | 55 | 102.1 |
| **实验3（rowIndex=2）** | 3.8 | 58 | 100.5 |

对应：
- `tableRowHeaders`: `["实验1", "实验2", "实验3"]`
- `tableColumnHeaders`: `["温度", "湿度", "压力"]`

---

## 二、教师端 — 创建/修改表格结构

### 2.1 接口说明

教师通过以下接口创建或修改表格类型数据收集步骤：

| 操作 | 接口 |
|---|---|
| 创建 | `POST /api/teacher/procedure/data-collection/create` |
| 更新 | `PUT /api/teacher/procedure/data-collection/update` |
| 插入 | `POST /api/teacher/procedure/data-collection/insert` |

三个接口的表格相关字段完全一致。

### 2.2 请求参数（表格相关字段）

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `dataType` | `Integer` | 是 | 固定传 `2`（表格类型） |
| `tableRowHeaders` | `List<String>` | 是 | 行表头名称列表，不可为空 |
| `tableColumnHeaders` | `List<String>` | 是 | 列表头名称列表，不可为空 |
| `tableCellAnswers` | `List<TableCellAnswer>` | 是 | 每个单元格的正确答案，不可为空 |
| `tableColumnTolerances` | `List<ColumnTolerance>` | 否 | 列级误差配置，可选 |
| `tolerance` | `Double` | 否 | 步骤级误差百分比（兜底），如 `5` 表示 ±5% |

### 2.3 TableCellAnswer 对象格式

教师设置单元格正确答案时使用：

```json
{
  "rowIndex": 0,
  "columnIndex": 0,
  "value": "3.5",
  "tolerance": 5.0
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `rowIndex` | `Integer` | 是 | 行索引（从 0 开始） |
| `columnIndex` | `Integer` | 是 | 列索引（从 0 开始） |
| `value` | `String` | 是 | 正确答案值 |
| `tolerance` | `Double` | 否 | 单元格级误差百分比（如 `5` 表示 ±5%），优先级最高 |

### 2.4 ColumnTolerance 对象格式

教师设置列级误差时使用：

```json
{
  "columnIndex": 0,
  "tolerance": 3.0
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `columnIndex` | `Integer` | 是 | 列索引（从 0 开始） |
| `tolerance` | `Double` | 是 | 列级误差百分比（如 `3` 表示 ±3%） |

### 2.5 教师创建完整请求示例

```json
{
  "dataType": 2,
  "tableRowHeaders": ["实验1", "实验2"],
  "tableColumnHeaders": ["温度", "湿度", "压力"],
  "tableCellAnswers": [
    {"rowIndex": 0, "columnIndex": 0, "value": "3.5"},
    {"rowIndex": 0, "columnIndex": 1, "value": "60", "tolerance": 10.0},
    {"rowIndex": 0, "columnIndex": 2, "value": "101.3"},
    {"rowIndex": 1, "columnIndex": 0, "value": "4.2"},
    {"rowIndex": 1, "columnIndex": 1, "value": "55"},
    {"rowIndex": 1, "columnIndex": 2, "value": "102.1"}
  ],
  "tableColumnTolerances": [
    {"columnIndex": 0, "tolerance": 5.0},
    {"columnIndex": 2, "tolerance": 3.0}
  ],
  "tolerance": 10.0
}
```

---

## 三、数据库存储格式

### 3.1 `data_collection.remark`（表格结构描述 + 误差配置）

```json
{
  "tableRowHeaders": ["实验1", "实验2"],
  "tableColumnHeaders": ["温度", "湿度", "压力"],
  "tableCellAnswers": [
    {"rowIndex": 0, "columnIndex": 0, "value": "3.5"},
    {"rowIndex": 0, "columnIndex": 1, "value": "60", "tolerance": 10.0},
    {"rowIndex": 0, "columnIndex": 2, "value": "101.3"},
    {"rowIndex": 1, "columnIndex": 0, "value": "4.2"},
    {"rowIndex": 1, "columnIndex": 1, "value": "55"},
    {"rowIndex": 1, "columnIndex": 2, "value": "102.1"}
  ],
  "columnTolerances": [
    {"columnIndex": 0, "tolerance": 5.0},
    {"columnIndex": 2, "tolerance": 3.0}
  ]
}
```

### 3.2 `data_collection.correct_answer`（正确答案 Map）

key 为 `"rowIndex-columnIndex"` 格式字符串，value 为答案值：

```json
{
  "0-0": "3.5",
  "0-1": "60",
  "0-2": "101.3",
  "1-0": "4.2",
  "1-1": "55",
  "1-2": "102.1"
}
```

### 3.3 `data_collection.tolerance`（步骤级误差）

独立列存储，类型 `Double`，如 `10.0` 表示 ±10%。作为兜底误差，仅在没有单元格级和列级误差时使用。

---

## 四、学生端 — 填写表格数值

### 4.1 接口说明

| 操作 | 接口 | 方法 |
|---|---|---|
| 提交 | `/api/student/procedure-submissions/data-collection/complete` | POST |
| 修改 | `/api/student/procedure-submissions/data-collection/update` | PUT |

### 4.2 学生提交参数

学生通过 `@RequestParam` 传入 `tableCellAnswers` 参数（JSON 字符串），格式为 **Map**：

```
tableCellAnswers: {"0-0":"3.5","0-1":"58","0-2":"101.0","1-0":"4.0","1-1":"56","1-2":"103.2"}
```

| 规则 | 说明 |
|---|---|
| key 格式 | `"rowIndex-columnIndex"`，如 `"0-0"`, `"1-2"` |
| value 格式 | 答案值字符串 |
| 必须覆盖所有单元格 | 学生必须填写表格中所有单元格 |

### 4.3 学生答案存储格式

学生答案存入 `student_experimental_procedure.answer` 列：

```json
{
  "type": "DATA_COLLECTION",
  "data": {
    "tableCellAnswers": {
      "0-0": "3.5",
      "0-1": "58",
      "0-2": "101.0",
      "1-0": "4.0",
      "1-1": "56",
      "1-2": "103.2"
    }
  }
}
```

---

## 五、自动判分 — 误差优先级

自动判分时，对每个单元格按以下优先级查找误差配置：

```
单元格级（tableCellAnswers[i].tolerance） > 列级（columnTolerances） > 步骤级（tolerance 列）
```

| 级别 | 来源 | key 格式 | 示例 |
|---|---|---|---|
| **单元格级** | `remark.tableCellAnswers[i].tolerance` | `"rowIndex-columnIndex"` | `"0-1"` → `10.0` |
| **列级** | `remark.columnTolerances[i].tolerance` | `columnIndex` 字符串 | `"0"` → `5.0` |
| **步骤级** | `data_collection.tolerance` 列 | 无 | `10.0` |

### 判分公式

对于数值类型答案，使用**百分比相对误差**：

```
|学生答案 - 正确答案| / |正确答案| ≤ 误差 / 100
```

- 误差为 `null` 或 `0` 时，执行精确匹配
- 正确答案为 `0` 时，执行精确匹配（避免除零）
- 非数值类型答案执行字符串精确匹配

### 判分流程

1. 从 `correct_answer` 列解析出 `Map<"rowIndex-columnIndex", 正确答案>`
2. 从 `remark` 列解析出单元格级误差 `Map<"rowIndex-columnIndex", 误差>` 和列级误差 `Map<"columnIndex", 误差>`
3. 遍历每个正确答案条目：
   - 查找单元格级误差 → 未配置则查找列级误差 → 未配置则使用步骤级误差
   - 比对学生答案与正确答案
4. 计算得分 = 正确数 / 总数 × 100（百分比制，保留两位小数）

---

## 六、响应格式 — 查询回显

### 6.1 教师查询步骤详情（未提交时）

返回 `tableRemark` 字段，包含表格结构描述：

```json
{
  "dataCollectionType": 2,
  "tableRemark": {
    "tableRowHeaders": ["实验1", "实验2"],
    "tableColumnHeaders": ["温度", "湿度", "压力"],
    "tableCellAnswers": [
      {"rowIndex": 0, "columnIndex": 0, "value": "3.5"},
      {"rowIndex": 0, "columnIndex": 1, "value": "60", "tolerance": 10.0}
    ],
    "columnTolerances": [
      {"columnIndex": 0, "tolerance": 5.0}
    ]
  },
  "tolerance": 10.0
}
```

### 6.2 教师/学生查询已提交详情

返回学生填写的 `tableCellAnswers` 列表（从 `answer` JSON 解析）：

```json
{
  "tableCellAnswers": [
    {"rowIndex": 0, "columnIndex": 0, "value": "3.5"},
    {"rowIndex": 0, "columnIndex": 1, "value": "58"},
    {"rowIndex": 1, "columnIndex": 0, "value": "4.0"}
  ]
}
```

---

## 七、格式速查表

| 场景 | 格式 | key 定位方式 |
|---|---|---|
| 教师设置单元格答案 | `List<TableCellAnswer>` | `rowIndex` + `columnIndex` 两个整型字段 |
| 教师设置列级误差 | `List<ColumnTolerance>` | `columnIndex` 整型字段 |
| remark 存储 | `TableRemarkDTO` (JSON) | 同上 |
| correct_answer 存储 | `Map<"rowIndex-columnIndex", 答案>` | 拼接字符串 key，如 `"0-1"` |
| 学生提交答案 | `Map<"rowIndex-columnIndex", 答案>` | 拼接字符串 key，如 `"0-1"` |
| 学生答案存储 | `Map<"rowIndex-columnIndex", 答案>` | 同上 |
| 响应回显 | `List<TableCellAnswer>` | `rowIndex` + `columnIndex` 两个整型字段 |
| 自动判分 — 单元格误差查找 | `Map<"rowIndex-columnIndex", 误差>` | 拼接字符串 key |
| 自动判分 — 列级误差查找 | `Map<"columnIndex", 误差>` | 列索引字符串，如 `"0"` |

---

## 八、禁止事项

1. **禁止**使用 `"A1"`, `"B2"` 等字母+数字组合定位单元格，统一使用 `rowIndex` + `columnIndex` 整型字段
2. **禁止**在 `correct_answer` 列中使用非 `"rowIndex-columnIndex"` 格式的 key
3. **禁止**学生提交时使用非 `"rowIndex-columnIndex"` 格式的 key
4. **禁止**在 `remark` JSON 中存储 `dataType`、`correctAnswer`、`tolerance` 等冗余字段
5. **禁止**跳过单元格——`tableCellAnswers` 必须覆盖表格所有行列组合
