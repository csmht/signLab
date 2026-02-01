package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生班级导入服务
 * 提供学生和班级信息批量导入的业务逻辑处理
 * 优化后的流程：
 * 1. 提取所有唯一班级，自动生成班级编号
 * 2. 构建班级编号 → 学生用户名列表的映射
 * 3. 批量插入所有学生
 * 4. 批量插入所有班级
 * 5. 批量插入所有绑定关系
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentClassImportService {

    private final AuthService authService;
    private final ClassService classService;
    private final StudentClassRelationService relationService;

    /**
     * 批量导入学生和班级信息（优化后的流程）
     *
     * @param dataList Excel数据列表
     * @return 导入结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchImportStudentClassResponse batchImportStudentWithClass(
            List<StudentClassImportExcel> dataList) {

        BatchImportStudentClassResponse response = new BatchImportStudentClassResponse();

        log.info("开始批量导入学生和班级，共 {} 条数据", dataList.size());

        // ========== 第一步：提取所有唯一班级 ==========
        Set<String> uniqueClassNames = dataList.stream()
                .map(StudentClassImportExcel::getClassName)
                .map(String::trim)
                .collect(Collectors.toSet());

        log.info("提取到 {} 个唯一班级", uniqueClassNames.size());

        // ========== 第二步：检查哪些班级已存在，生成班级编号映射 ==========
        Map<String, String> classNameToCodeMap = new LinkedHashMap<>();
        List<String> newClassNames = new ArrayList<>();

        // 先检查每个班级是否已存在
        for (String className : uniqueClassNames) {
            QueryWrapper<com.example.demo.pojo.entity.Class> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_name", className);
            com.example.demo.pojo.entity.Class existingClass = classService.getOne(queryWrapper);

            if (existingClass != null) {
                // 班级已存在，使用已有编号
                classNameToCodeMap.put(className, existingClass.getClassCode());
                log.info("班级 [{}] 已存在，使用编号: {}", className, existingClass.getClassCode());
            } else {
                // 班级不存在，记录下来稍后生成编号
                newClassNames.add(className);
            }
        }

        // 一次性生成所有新班级的编号（确保连续）
        if (!newClassNames.isEmpty()) {
            // 获取当前最大班级ID
            QueryWrapper<com.example.demo.pojo.entity.Class> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("id");
            queryWrapper.last("LIMIT 1");
            com.example.demo.pojo.entity.Class lastClass = classService.getOne(queryWrapper);

            int nextNum = 1;
            if (lastClass != null && lastClass.getId() != null) {
                // 直接使用ID作为编号
                nextNum = lastClass.getId().intValue() + 1;
            }

            // 为每个新班级生成连续编号
            for (String className : newClassNames) {
                String classCode = String.format("CLASS%06d", nextNum++);
                classNameToCodeMap.put(className, classCode);
                log.info("班级 [{}] 生成编号: {}", className, classCode);
            }
        }

        // ========== 第三步：构建班级编号 → 学生信息列表的映射 ==========
        Map<String, List<StudentClassImportExcel>> classToStudentsMap = dataList.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getClassName().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // ========== 第四步：批量插入所有班级 ==========
        List<BatchAddClassRequest.ClassInfo> newClasses = new ArrayList<>();
        for (Map.Entry<String, String> entry : classNameToCodeMap.entrySet()) {
            String className = entry.getKey();
            String classCode = entry.getValue();

            // 检查班级是否真的需要新建
            QueryWrapper<com.example.demo.pojo.entity.Class> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_code", classCode);
            com.example.demo.pojo.entity.Class existing = classService.getOne(queryWrapper);

            if (existing == null) {
                BatchAddClassRequest.ClassInfo classInfo = new BatchAddClassRequest.ClassInfo();
                classInfo.setClassCode(classCode);
                classInfo.setClassName(className);
                newClasses.add(classInfo);
            } else {
                response.setClassDuplicateCount(response.getClassDuplicateCount() + 1);
                log.info("班级已存在，跳过: {}", classCode);
            }
        }

        // 批量创建班级
        if (!newClasses.isEmpty()) {
            BatchAddClassRequest classRequest = new BatchAddClassRequest();
            classRequest.setClasses(newClasses);

            BatchAddClassResponse classResponse = classService.batchAddClasses(classRequest);
            response.setClassSuccessCount(response.getClassSuccessCount() + classResponse.getSuccessCount());
            response.setClassFailCount(response.getClassFailCount() + classResponse.getFailCount());

            log.info("批量导入班级完成 - 成功:{} 失败:{}",
                    classResponse.getSuccessCount(), classResponse.getFailCount());
        }

        // ========== 第五步：批量插入所有学生 ==========
        // 定义批次大小
        final int STUDENT_BATCH_SIZE = 100;

        // 收集所有需要导入的学生信息
        List<BatchAddUserRequest> allStudentRequests = new ArrayList<>();
        // 用于记录学生所属的班级（用户名 -> 班级编号）
        Map<String, String> studentToClassMap = new LinkedHashMap<>();

        for (Map.Entry<String, List<StudentClassImportExcel>> entry : classToStudentsMap.entrySet()) {
            String className = entry.getKey();
            String classCode = classNameToCodeMap.get(className);
            List<StudentClassImportExcel> students = entry.getValue();

            for (StudentClassImportExcel excelData : students) {
                // 转换为用户请求
                BatchAddUserRequest userRequest = new BatchAddUserRequest();
                userRequest.setUsername(excelData.getUsername().trim());
                userRequest.setName(excelData.getName().trim());
                userRequest.setRole("student");
                userRequest.setDepartment(excelData.getDepartment() != null ?
                        excelData.getDepartment().trim() : null);
                userRequest.setMajor(excelData.getMajor() != null ?
                        excelData.getMajor().trim() : null);

                allStudentRequests.add(userRequest);
                studentToClassMap.put(userRequest.getUsername(), classCode);
            }
        }

        log.info("准备导入 {} 个学生，按批次处理（每批 {} 个）", allStudentRequests.size(), STUDENT_BATCH_SIZE);

        // 按批次导入学生
        List<List<BatchAddUserRequest>> studentBatches = partitionList(allStudentRequests, STUDENT_BATCH_SIZE);

        for (List<BatchAddUserRequest> batch : studentBatches) {
            try {
                BatchAddUserResponse userResponse = authService.batchAddUsers(batch);

                response.setStudentSuccessCount(response.getStudentSuccessCount() + userResponse.getSuccessCount());
                response.setStudentDuplicateCount(response.getStudentDuplicateCount() + userResponse.getDuplicateCount());
                response.setStudentFailCount(response.getStudentFailCount() + userResponse.getFailCount());

                // 收集失败信息
                if (userResponse.getFailedUsers() != null) {
                    userResponse.getFailedUsers().forEach(fail -> {
                        response.getErrorMessages().add(String.format(
                                "学生导入失败 [用户名:%s] - %s",
                                fail.getUsername(), fail.getReason()
                        ));
                    });
                }

                log.info("批次导入学生完成 - 成功:{} 重复:{} 失败:{}",
                        userResponse.getSuccessCount(), userResponse.getDuplicateCount(), userResponse.getFailCount());

            } catch (Exception e) {
                log.error("批次导入学生异常", e);
                response.setStudentFailCount(response.getStudentFailCount() + batch.size());
                response.getErrorMessages().add("批次导入学生异常: " + e.getMessage());
            }
        }

        // ========== 第六步：按班级收集学生用户名，准备批量绑定 ==========
        Map<String, List<String>> classToStudentUsernamesMap = new LinkedHashMap<>();

        for (Map.Entry<String, List<StudentClassImportExcel>> entry : classToStudentsMap.entrySet()) {
            String className = entry.getKey();
            String classCode = classNameToCodeMap.get(className);
            List<StudentClassImportExcel> students = entry.getValue();

            List<String> studentUsernames = new ArrayList<>();
            for (StudentClassImportExcel excelData : students) {
                studentUsernames.add(excelData.getUsername().trim());
            }

            classToStudentUsernamesMap.put(classCode, studentUsernames);
        }

        log.info("准备为 {} 个班级绑定学生关系", classToStudentUsernamesMap.size());

        // ========== 第七步：批量插入所有绑定关系（按班级分批）==========
        for (Map.Entry<String, List<String>> entry : classToStudentUsernamesMap.entrySet()) {
            String classCode = entry.getKey();
            List<String> studentUsernames = entry.getValue();

            if (studentUsernames.isEmpty()) {
                log.info("班级 [{}] 没有需要绑定的学生", classCode);
                continue;
            }

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

                log.info("班级 [{}] 绑定完成 - 成功:{} 失败:{}",
                        classCode, bindResponse.getSuccessCount(), bindResponse.getFailCount());

            } catch (Exception e) {
                log.error("批量绑定学生到班级异常: {}", classCode, e);
                response.setBindFailCount(response.getBindFailCount() + studentUsernames.size());
                response.getErrorMessages().add("批量绑定异常 [班级:" + classCode + "] - " + e.getMessage());
            }
        }

        log.info("批量导入完成 - 学生成功:{} 重复:{} 失败:{} | 班级成功:{} 重复:{} 失败:{} | 绑定成功:{} 失败:{}",
                response.getStudentSuccessCount(), response.getStudentDuplicateCount(), response.getStudentFailCount(),
                response.getClassSuccessCount(), response.getClassDuplicateCount(), response.getClassFailCount(),
                response.getBindSuccessCount(), response.getBindFailCount());

        return response;
    }

    /**
     * 将列表分割成指定大小的批次
     *
     * @param list     原始列表
     * @param batchSize 批次大小
     * @param <T>      列表元素类型
     * @return 批次列表
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}
