# 实验签到管理系统 - Claude 开发指南

> 本文档专为此项目设计，用于指导 Claude 助手理解项目架构、开发规范和注意事项

---

## 一、项目概述

### 1.1 项目定位
**高校实验课签到与流程管理系统**，主要用于：
- 实验课学生签到管理（二维码扫码签到）
- 实验步骤流程控制（预习 → 签到 → 实验 → 报告 → 成绩）
- 学生数据采集（答案、文件、照片）
- 题库与预习测试管理
- 成绩管理与导出

### 1.2 技术栈
- **后端框架**：Spring Boot 3.5.5
- **数据库**：MySQL + MyBatis Plus 3.5.9
- **安全认证**：JWT + Spring Security
- **自动建表**：AutoTable 2.5.0
- **微信集成**：微信公众号 API
- **文件处理**：EasyExcel、ZXing（二维码）、Apache POI

### 1.3 核心实体关系
```
User（用户）
  ↓
Class（班级）←→ StudentClassRelation（学生班级关系）
  ↓
ClassExperiment（班级实验关联）← Experiment（实验）
  ↓
ExperimentalProcedure（实验步骤）
  ↓
ProcedureTopic（步骤题目）← Topic（题目）← Tag（标签）
  ↓
StudentExperimentalProcedure（学生实验进度）
  ↓
DataCollection（数据采集）← AnswerFile/AnswerPhoto（答案文件/照片）
```

---

## 二、项目结构规范

### 2.1 包结构
```
com.example.demo/
├── annotation/          # 自定义注解（如 @RequireRole）
├── aspect/             # AOP 切面
├── config/             # 配置类（Security、MyBatis 等）
├── controller/         # 控制器层
│   ├── student/       # 学生端接口
│   └── teacher/       # 教师端接口
├── pojo/              # 数据对象
│   ├── entity/        # 实体类（对应数据库表）
│   ├── request/       # 请求参数对象
│   ├── response/      # 响应结果对象
│   ├── vo/            # 视图对象
│   ├── dto/           # 数据传输对象
│   └── excel/         # Excel 导入导出对象
├── service/           # 服务层接口和实现
├── mapper/            # MyBatis 映射器
├── util/              # 工具类
├── enums/             # 枚举类
├── exception/         # 自定义异常
├── filter/            # 过滤器
├── interceptor/       # 拦截器
└── listener/          # 监听器
```

### 2.2 命名规范

#### 实体类（Entity）
- 位置：`pojo/entity/`
- 命名：使用数据库表名的驼峰命名
- 示例：`AttendanceRecord.java`、`ExperimentalProcedure.java`

#### 请求对象（Request）
- 位置：`pojo/request/`
- 命名：`{操作}Request`
- 示例：`CreateClassRequest.java`、`UpdateAttendanceRequest.java`

#### 响应对象（Response）
- 位置：`pojo/response/`
- 命名：`{实体}Response` 或 `{操作}Response`
- 示例：`ClassResponse.java`、`LoginResponse.java`

#### 控制器（Controller）
- 位置：`controller/student/` 或 `controller/teacher/`
- 命名：`{功能}Controller`
- 示例：`StudentAttendanceController.java`、`ClassController.java`

#### 服务层（Service）
- 接口位置：`service/`
- 实现位置：`service/impl/`
- 命名：`{实体}Service` 和 `{实体}ServiceImpl`

---

## 三、接口开发规范

### 3.1 统一响应格式

所有接口必须使用 `ApiResponse` 包装响应结果：

```java
// 成功响应
ApiResponse.success(data, "操作成功");

// 失败响应
ApiResponse.error(400, "错误信息");

// 控制器返回示例
@PostMapping("/create")
public ApiResponse<ClassResponse> create(@RequestBody CreateClassRequest request) {
    try {
        ClassResponse response = classService.create(request);
        return ApiResponse.success(response, "创建成功");
    } catch (Exception e) {
        return ApiResponse.error(500, "创建失败: " + e.getMessage());
    }
}
```

### 3.2 接口命名规范

#### RESTful 风格
| 操作 | HTTP 方法 | 路径示例 |
|------|----------|---------|
| 创建 | POST | `/api/teacher/class` |
| 查询单个 | GET | `/api/teacher/class/{id}` |
| 查询列表 | GET/POST | `/api/teacher/class/list` 或 `/api/teacher/class/query` |
| 更新 | PUT | `/api/teacher/class/{id}` |
| 删除 | DELETE | `/api/teacher/class/{id}` |

#### 路径规范
- 教师端：`/api/teacher/{模块}/{操作}`
- 学生端：`/api/student/{模块}/{操作}`
- 通用接口：`/api/{模块}/{操作}`

### 3.3 权限控制

使用 `@RequireRole` 注解控制接口访问权限：

```java
@RequireRole(value = UserRole.TEACHER)
@PostMapping("/create")
public ApiResponse<Class> create(@RequestBody CreateClassRequest request) {
    // 仅教师可访问
}

@RequireRole(value = UserRole.STUDENT)
@GetMapping("/my-records")
public ApiResponse<List<AttendanceRecord>> getMyRecords() {
    // 仅学生可访问
}
```

### 3.4 获取当前用户

从 SecurityContext 获取当前登录用户：

```java
String currentUsername = SecurityUtil.getCurrentUsername()
    .orElseThrow(() -> new BusinessException(401, "未登录"));
```

### 3.5 跨域配置

所有控制器需要添加跨域注解：

```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teacher/class")
public class ClassController {
    // ...
}
```

---

## 四、数据库设计规范

### 4.1 实体类注解规范

使用 MyBatis Plus 注解：

```java
@Data
@TableName("attendance_record")
public class AttendanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_username")
    private String studentUsername;

    @TableField("class_experiment_id")
    private Long classExperimentId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
```

### 4.2 通用字段规范

所有实体类应包含以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键（自增） |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### 4.3 外键命名规范

外键字段使用 `{关联表}_{关联字段}` 格式：

- `student_username`（关联 User 表的 username）
- `class_experiment_id`（关联 ClassExperiment 表的 id）
- `experiment_id`（关联 Experiment 表的 id）

---

## 五、业务逻辑规范

### 5.1 实验流程控制

**标准流程**：预习 → 签到 → 实验步骤 → 数据提交 → 实验报告 → 成绩

实现步骤控制时需要：
1. 检查上一步是否完成
2. 验证当前步骤是否解锁
3. 记录学生进度到 `StudentExperimentalProcedure` 表

```java
// 示例：检查学生是否可以进入当前步骤
public boolean canAccessProcedure(String studentUsername, Long procedureId) {
    // 1. 查询实验的所有步骤
    // 2. 按顺序排序
    // 3. 检查当前步骤的前置步骤是否完成
    // 4. 返回是否可以访问
}
```

### 5.2 签到逻辑

**签到状态**：
- `PRESENT`：已签到
- `ABSENT`：未签到
- `LATE`：迟到
- `LEAVE`：请假

**跨班签到识别**：
- 检查学生的 `classCode` 是否与签到的 `classCode` 一致
- 不一致则标记为跨班签到

### 5.3 成绩计算

**权重关系**：
- 课程成绩 = Σ(实验成绩 × 实验权重)
- 实验成绩 = Σ(步骤成绩 × 步骤权重)

实现自动计算时：
1. 从配置表获取权重
2. 查询学生各步骤成绩
3. 按权重加权计算
4. 保存到成绩表

---

## 六、开发注意事项

### 6.1 必须遵守的规则

1. **所有接口必须返回 `ApiResponse`**
   - 不允许直接返回对象
   - 统一成功和失败的响应格式

2. **所有控制器必须添加权限注解**
   - 公开接口除外（如登录）
   - 使用 `@RequireRole` 指定角色

3. **所有异常必须捕获并处理**
   - 使用 `try-catch` 包裹业务逻辑
   - 返回明确的错误信息

4. **敏感操作需要记录日志**
   - 使用 `@Slf4j` 注解
   - 关键操作记录 `log.info()` 或 `log.error()`

5. **数据库操作使用 Service 层**
   - Controller 不直接调用 Mapper
   - 业务逻辑在 Service 层实现

### 6.2 性能优化建议

1. **批量操作使用批量接口**
   - 批量插入：`saveBatch()`
   - 批量查询：使用 `in` 条件

2. **避免 N+1 查询**
   - 使用 MyBatis Plus 的 `@TableField(select = false)`
   - 或使用关联查询

3. **分页查询**
   - 使用 MyBatis Plus 的 `Page` 对象
   - 返回 `PageResponse` 包装结果

### 6.3 安全注意事项

1. **密码加密**
   - 使用 BCrypt 加密存储
   - 不在日志中打印密码

2. **SQL 注入防护**
   - 使用 MyBatis Plus 的方法（不手写 SQL）
   - 或使用 `#{param}` 占位符

3. **权限验证**
   - 所有接口必须验证用户身份
   - 检查用户是否有权限操作该资源

---

## 七、常见问题与解决方案

### 7.1 如何创建新接口？

**步骤**：
1. 在 `pojo/request/` 创建请求对象
2. 在 `pojo/response/` 创建响应对象
3. 在 `service/` 创建服务接口和实现
4. 在 `controller/student/` 或 `controller/teacher/` 创建控制器
5. 使用 `@RequireRole` 添加权限控制
6. 返回 `ApiResponse` 包装结果

**示例**：
```java
// 1. 请求对象
@Data
public class CreateTopicRequest {
    private String title;
    private String content;
    private String type;
}

// 2. 响应对象
@Data
public class TopicResponse {
    private Long id;
    private String title;
    private String content;
}

// 3. 服务层
public interface TopicService {
    TopicResponse create(CreateTopicRequest request);
}

// 4. 控制器
@PostMapping
@RequireRole(value = UserRole.TEACHER)
public ApiResponse<TopicResponse> create(@RequestBody CreateTopicRequest request) {
    try {
        TopicResponse response = topicService.create(request);
        return ApiResponse.success(response, "创建成功");
    } catch (Exception e) {
        return ApiResponse.error(500, "创建失败: " + e.getMessage());
    }
}
```

### 7.2 如何添加新的实体类？

**步骤**：
1. 在 `pojo/entity/` 创建实体类
2. 使用 `@TableName` 指定表名
3. 使用 `@TableId` 和 `@TableField` 标注字段
4. 在 `mapper/` 创建 Mapper 接口
5. 使用 `@Mapper` 注解
6. 在 `service/` 创建服务接口和实现

**示例**：
```java
// 1. 实体类
@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

// 2. Mapper
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}

// 3. Service
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {
    private final TopicMapper topicMapper;

    @Override
    public TopicResponse create(CreateTopicRequest request) {
        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setContent(request.getContent());
        topicMapper.insert(topic);
        // ... 转换为响应对象
    }
}
```

### 7.3 如何实现分页查询？

**使用 MyBatis Plus 的 `Page` 对象**：
```java
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;

    @Override
    public PageResponse<ClassResponse> queryClasses(ClassQueryRequest request) {
        // 1. 创建分页对象
        Page<Class> page = new Page<>(request.getPage(), request.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(request.getClassCode())) {
            wrapper.eq(Class::getClassCode, request.getClassCode());
        }
        if (StringUtils.isNotBlank(request.getClassName())) {
            wrapper.like(Class::getClassName, request.getClassName());
        }

        // 3. 执行分页查询
        Page<Class> result = classMapper.selectPage(page, wrapper);

        // 4. 转换为响应对象
        List<ClassResponse> responses = result.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        // 5. 返回分页结果
        return new PageResponse<>(
            responses,
            result.getTotal(),
            result.getCurrent(),
            result.getSize()
        );
    }
}
```

### 7.4 如何处理事务？

**使用 `@Transactional` 注解**：
```java
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchBindClasses(BatchBindClassesRequest request) {
        // 多个数据库操作
        // 如果任何一个失败，所有操作回滚
        classExperimentService.batchInsert(request.getClassExperimentList());
        studentClassRelationService.batchUpdate(request.getStudentClassList());
    }
}
```

---

## 八、待实现功能清单

### 8.1 题库与预习系统（P0）
- [ ] 题目 CRUD 接口
- [ ] 标签管理接口
- [ ] 题目标签绑定
- [ ] 随机抽题算法
- [ ] 学生答题记录
- [ ] 预习测试接口

### 8.2 课程进度流程控制（P0）
- [ ] 实验步骤顺序控制
- [ ] 学生进度查询
- [ ] 步骤解锁验证
- [ ] 进度状态更新

### 8.3 成绩权重计算（P0）
- [ ] 权重配置接口
- [ ] 自动计算成绩
- [ ] 成绩导出 Excel

### 8.4 实验报告管理（P1）
- [ ] 报告提交接口
- [ ] 教师批改接口
- [ ] 批改记录查询

### 8.5 视频管理（P2）
- [ ] 视频上传
- [ ] 视频播放接口
- [ ] 播放进度记录

### 8.6 动态表格（P1）
- [ ] 表格配置接口
- [ ] 动态表格数据存储

### 8.7 OCR 识别（P2）
- [ ] 图片上传
- [ ] OCR 服务集成
- [ ] 识别结果返回

### 8.8 消息提醒（P1）
- [ ] 提醒规则配置
- [ ] 定时任务扫描
- [ ] 微信消息推送

---

## 九、快速参考

### 9.1 常用注解
| 注解 | 用途 |
|------|------|
| `@RestController` | 标记为控制器 |
| `@RequestMapping` | 定义接口路径 |
| `@RequireRole` | 权限控制 |
| `@CrossOrigin` | 跨域配置 |
| `@Service` | 标记为服务层 |
| `@Mapper` | 标记为 Mapper |
| `@Transactional` | 事务管理 |

### 9.2 常用工具类
| 工具类 | 位置 | 用途 |
|--------|------|------|
| `SecurityUtil` | `util/` | 获取当前用户信息 |
| `JwtUtil` | `util/` | JWT Token 生成和验证 |
| `QrCodeGenerator` | `util/` | 二维码生成 |

### 9.3 常用枚举
| 枚举 | 位置 | 说明 |
|------|------|------|
| `UserRole` | `enums/` | 用户角色（STUDENT、TEACHER、ADMIN） |
| `AttendanceStatus` | `enums/` | 签到状态 |

---

## 十、重要提醒

### ⚠️ 禁止事项
1. **禁止在 Controller 中直接调用 Mapper**
2. **禁止返回非 `ApiResponse` 格式的响应**
3. **禁止硬编码配置数据**（应使用配置文件或数据库）
4. **禁止在代码中写死中文文本**（应使用常量或配置文件）
5. **禁止在生产环境输出敏感信息**（密码、Token 等）

### ✅ 推荐做法
1. 使用 Builder 模式构建复杂对象
2. 使用 Lambda 表达式简化代码
3. 使用 Stream API 处理集合
4. 使用 Lombok 注解减少样板代码
5. 添加详细的注释和 JavaDoc

---

**文档版本**：v1.0
**最后更新**：2026-01-26
**维护者**：项目开发团队