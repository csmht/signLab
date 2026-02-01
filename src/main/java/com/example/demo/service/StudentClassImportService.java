package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.excel.StudentClassImportExcel;
import com.example.demo.pojo.request.BatchAddClassRequest;
import com.example.demo.pojo.request.BatchAddUserRequest;
import com.example.demo.pojo.request.BatchBindStudentsRequest;
import com.example.demo.pojo.response.BatchAddClassResponse;
import com.example.demo.pojo.response.BatchAddUserResponse;
import com.example.demo.pojo.response.BatchBindStudentsResponse;
import com.example.demo.pojo.response.BatchImportStudentClassResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生班级导入服务
 * 提供学生和班级信息批量导入的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentClassImportService {

    private final AuthService authService;
    private final ClassService classService;
    private final StudentClassRelationService relationService;
    private final UserMapper userMapper;

    /**
     * 批量导入学生和班级信息
     *
     * @param dataList Excel数据列表
     * @return 导入结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchImportStudentClassResponse batchImportStudentWithClass(
            List<StudentClassImportExcel> dataList) {

        BatchImportStudentClassResponse response = new BatchImportStudentClassResponse();

        // 1. 按班级分组（使用班级编号+班级名称作为分组键）
        Map<String, List<StudentClassImportExcel>> classGroupMap = dataList.stream()
                .collect(Collectors.groupingBy(data -> {
                    String classCode = (data.getClassCode() != null && !data.getClassCode().trim().isEmpty())
                            ? data.getClassCode().trim() : "AUTO_GENERATE";
                    return classCode + "|" + data.getClassName().trim();
                }));

        log.info("导入数据已按班级分组，共 {} 个班级", classGroupMap.size());

        // 2. 处理每个班级及其学生
        for (Map.Entry<String, List<StudentClassImportExcel>> entry : classGroupMap.entrySet()) {
            String classKey = entry.getKey();
            List<StudentClassImportExcel> studentList = entry.getValue();

            // 解析班级信息
            String[] parts = classKey.split("\\|");
            String inputClassCode = "AUTO_GENERATE".equals(parts[0]) ? null : parts[0];
            String className = parts[1];

            log.info("处理班级 [{}]，包含 {} 个学生", className, studentList.size());

            // 3. 导入班级
            String finalClassCode = importClass(inputClassCode, className, response);

            // 4. 导入学生
            List<String> studentUsernames = importStudents(studentList, response, finalClassCode);

            // 5. 绑定学生到班级
            if (finalClassCode != null && !studentUsernames.isEmpty()) {
                bindStudentsToClass(finalClassCode, studentUsernames, response);
            }
        }

        log.info("批量导入完成 - 学生成功:{} 重复:{} 失败:{} | 班级成功:{} 重复:{} 失败:{} | 绑定成功:{} 失败:{}",
                response.getStudentSuccessCount(), response.getStudentDuplicateCount(), response.getStudentFailCount(),
                response.getClassSuccessCount(), response.getClassDuplicateCount(), response.getClassFailCount(),
                response.getBindSuccessCount(), response.getBindFailCount());

        return response;
    }

    /**
     * 导入班级
     *
     * @param classCode 班级编号（可能为null）
     * @param className 班级名称
     * @param response  响应对象
     * @return 最终的班级编号（导入成功或已存在）
     */
    private String importClass(String classCode, String className,
                               BatchImportStudentClassResponse response) {
        try {
            // 如果班级编号为空，自动生成
            if (classCode == null) {
                classCode = classService.generateClassCode();
                log.info("自动生成班级编号: {}", classCode);
            }

            // 检查班级是否已存在
            com.example.demo.pojo.entity.Class existingClass = classService.getByClassCode(classCode);
            if (existingClass != null) {
                log.info("班级已存在，跳过导入: {}", classCode);
                response.setClassDuplicateCount(response.getClassDuplicateCount() + 1);
                return classCode; // 班级已存在，直接使用
            }

            // 创建班级
            BatchAddClassRequest.ClassInfo classInfo = new BatchAddClassRequest.ClassInfo();
            classInfo.setClassCode(classCode);
            classInfo.setClassName(className);

            BatchAddClassRequest classRequest = new BatchAddClassRequest();
            classRequest.setClasses(List.of(classInfo));

            BatchAddClassResponse classResponse = classService.batchAddClasses(classRequest);

            if (classResponse.getSuccessCount() > 0) {
                log.info("班级导入成功: {}", classCode);
                response.setClassSuccessCount(response.getClassSuccessCount() + 1);
                return classCode;
            } else {
                log.warn("班级导入失败: {}", className);
                response.setClassFailCount(response.getClassFailCount() + 1);
                response.getErrorMessages().add("班级创建失败: " + className);
                return null;
            }

        } catch (Exception e) {
            log.error("导入班级异常: {}", className, e);
            response.setClassFailCount(response.getClassFailCount() + 1);
            response.getErrorMessages().add("班级导入异常: " + className + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 导入学生
     *
     * @param studentList 学生列表
     * @param response    响应对象
     * @param classCode   班级编号
     * @return 成功导入或已存在的学生用户名列表
     */
    private List<String> importStudents(List<StudentClassImportExcel> studentList,
                                       BatchImportStudentClassResponse response,
                                       String classCode) {
        List<String> successUsernames = new ArrayList<>();

        for (StudentClassImportExcel excelData : studentList) {
            try {
                // 转换为用户请求
                BatchAddUserRequest userRequest = new BatchAddUserRequest();
                userRequest.setUsername(excelData.getUsername().trim());
                userRequest.setName(excelData.getName().trim());
                userRequest.setRole("student"); // 固定为学生角色
                userRequest.setDepartment(excelData.getDepartment() != null ?
                        excelData.getDepartment().trim() : null);
                userRequest.setMajor(excelData.getMajor() != null ?
                        excelData.getMajor().trim() : null);

                // 检查学生是否已存在
                User existingUser = getUserByUsername(userRequest.getUsername());
                if (existingUser != null) {
                    log.info("学生已存在，跳过导入: {}", userRequest.getUsername());
                    response.setStudentDuplicateCount(response.getStudentDuplicateCount() + 1);
                    // 学生已存在，仍可绑定班级
                    successUsernames.add(userRequest.getUsername());
                    continue;
                }

                // 导入学生
                BatchAddUserResponse userResponse = authService.batchAddUsers(List.of(userRequest));

                if (userResponse.getSuccessCount() > 0) {
                    log.info("学生导入成功: {}", userRequest.getUsername());
                    response.setStudentSuccessCount(response.getStudentSuccessCount() + 1);
                    successUsernames.add(userRequest.getUsername());
                } else {
                    log.warn("学生导入失败: {}", excelData.getUsername());
                    response.setStudentFailCount(response.getStudentFailCount() + 1);
                    response.getErrorMessages().add(String.format(
                            "学生导入失败 [用户名:%s, 姓名:%s]",
                            excelData.getUsername(), excelData.getName()
                    ));
                }

            } catch (Exception e) {
                log.error("导入学生异常: {}", excelData.getUsername(), e);
                response.setStudentFailCount(response.getStudentFailCount() + 1);
                response.getErrorMessages().add(String.format(
                        "学生导入异常 [用户名:%s, 姓名:%s] - %s",
                        excelData.getUsername(), excelData.getName(), e.getMessage()
                ));
            }
        }

        return successUsernames;
    }

    /**
     * 批量绑定学生到班级
     *
     * @param classCode        班级编号
     * @param studentUsernames 学生用户名列表
     * @param response         响应对象
     */
    private void bindStudentsToClass(String classCode, List<String> studentUsernames,
                                    BatchImportStudentClassResponse response) {
        try {
            BatchBindStudentsRequest bindRequest = new BatchBindStudentsRequest();
            bindRequest.setClassCode(classCode);
            bindRequest.setStudentUsernames(studentUsernames);

            BatchBindStudentsResponse bindResponse =
                    relationService.batchBindStudents(bindRequest);

            response.setBindSuccessCount(response.getBindSuccessCount() + bindResponse.getSuccessCount());
            response.setBindFailCount(response.getBindFailCount() + bindResponse.getFailCount());

            // 收集绑定失败的错误信息
            if (bindResponse.getFailList() != null) {
                bindResponse.getFailList().forEach(fail -> {
                    response.getErrorMessages().add(String.format(
                            "绑定失败 [用户名:%s, 班级:%s] - %s",
                            fail.getStudentUsername(), classCode, fail.getMessage()
                    ));
                });
            }

            log.info("绑定完成 - 成功:{} 失败:{}", bindResponse.getSuccessCount(), bindResponse.getFailCount());

        } catch (Exception e) {
            log.error("批量绑定学生到班级异常: {}", classCode, e);
            response.setBindFailCount(response.getBindFailCount() + studentUsernames.size());
            response.getErrorMessages().add("批量绑定异常 [班级:" + classCode + "] - " + e.getMessage());
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    private User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
}
