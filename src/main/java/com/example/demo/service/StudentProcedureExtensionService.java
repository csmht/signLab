package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentProcedureExtensionMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.StudentProcedureExtension;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.request.teacher.ExtensionQueryRequest;
import com.example.demo.pojo.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生步骤时间延长服务
 * 提供步骤时间延长的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcedureExtensionService extends ServiceImpl<StudentProcedureExtensionMapper, StudentProcedureExtension> {

    private final ExperimentalProcedureService experimentalProcedureService;
    private final UserMapper userMapper;

    /**
     * 计算延长后的结束时间（核心方法）
     * 查询学生延长时间并累加到原始结束时间
     *
     * @param studentUsername 学生用户名
     * @param procedureId     实验步骤ID
     * @param baseEndTime     原始结束时间
     * @return 延长后的结束时间（无延长记录则返回原时间）
     */
    public LocalDateTime calculateExtendedEndTime(
            String studentUsername,
            Long procedureId,
            LocalDateTime baseEndTime) {

        if (baseEndTime == null) {
            return null;
        }

        Integer extendedMinutes = getExtendedMinutes(studentUsername, procedureId);
        if (extendedMinutes > 0) {
            return baseEndTime.plusMinutes(extendedMinutes);
        }

        return baseEndTime;
    }

    /**
     * 查询学生延长时间
     *
     * @param studentUsername 学生用户名
     * @param procedureId     实验步骤ID
     * @return 延长时间（分钟），无记录返回0
     */
    public Integer getExtendedMinutes(String studentUsername, Long procedureId) {
        LambdaQueryWrapper<StudentProcedureExtension> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProcedureExtension::getStudentUsername, studentUsername)
               .eq(StudentProcedureExtension::getExperimentalProcedureId, procedureId);
        StudentProcedureExtension extension = getOne(wrapper);

        if (extension != null && extension.getExtendedMinutes() != null) {
            return extension.getExtendedMinutes();
        }
        return 0;
    }

    /**
     * 批量设置学生步骤延长时间
     * 如果学生已有该步骤的延长记录，将返回错误
     *
     * @param procedureId       实验步骤ID
     * @param studentUsernames  学生用户名列表
     * @param extendedMinutes   延长时间（分钟）
     * @param teacherUsername   开通教师用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchExtend(
            Long procedureId,
            List<String> studentUsernames,
            Integer extendedMinutes,
            String teacherUsername) {

        if (extendedMinutes == null || extendedMinutes < 0) {
            throw new BusinessException(400, "延长时间必须为非负整数");
        }

        // 1. 批量查询已存在的记录
        LambdaQueryWrapper<StudentProcedureExtension> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentProcedureExtension::getExperimentalProcedureId, procedureId)
                    .in(StudentProcedureExtension::getStudentUsername, studentUsernames);
        List<StudentProcedureExtension> existingRecords = list(queryWrapper);

        // 2. 如果存在记录，返回错误
        if (!existingRecords.isEmpty()) {
            StudentProcedureExtension existing = existingRecords.get(0);
            throw new BusinessException(400, String.format(
                    "学生%s已有步骤%d的延长记录，需要删除后再添加",
                    existing.getStudentUsername(), procedureId));
        }

        // 3. 批量创建新记录
        List<StudentProcedureExtension> toInsert = new ArrayList<>();
        for (String studentUsername : studentUsernames) {
            StudentProcedureExtension extension = new StudentProcedureExtension();
            extension.setStudentUsername(studentUsername);
            extension.setExperimentalProcedureId(procedureId);
            extension.setExtendedMinutes(extendedMinutes);
            extension.setTeacherUsername(teacherUsername);
            toInsert.add(extension);
        }

        // 4. 批量插入
        saveBatch(toInsert);

        log.info("教师 {} 为 {} 名学生设置步骤 {} 延长时间 {} 分钟",
                teacherUsername, studentUsernames.size(), procedureId, extendedMinutes);
    }

    /**
     * 更新延长时间
     *
     * @param id              延长记录ID
     * @param extendedMinutes 延长时间（分钟）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateExtension(Long id, Integer extendedMinutes) {
        StudentProcedureExtension extension = getById(id);
        if (extension == null) {
            throw new BusinessException(404, "延长记录不存在");
        }

        if (extendedMinutes == null || extendedMinutes < 0) {
            throw new BusinessException(400, "延长时间必须为非负整数");
        }

        extension.setExtendedMinutes(extendedMinutes);
        updateById(extension);

        log.info("更新延长记录 {}，延长时间：{} 分钟", id, extendedMinutes);
    }

    /**
     * 删除延长记录
     *
     * @param id 延长记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteExtension(Long id) {
        StudentProcedureExtension extension = getById(id);
        if (extension == null) {
            throw new BusinessException(404, "延长记录不存在");
        }

        removeById(id);
        log.info("删除延长记录 {}", id);
    }

    /**
     * 分页筛选查询延长记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    public PageResponse<StudentProcedureExtension> queryExtensions(ExtensionQueryRequest request) {
        LambdaQueryWrapper<StudentProcedureExtension> wrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        if (StringUtils.hasText(request.getStudentUsername())) {
            wrapper.like(StudentProcedureExtension::getStudentUsername, request.getStudentUsername());
        }
        if (StringUtils.hasText(request.getTeacherUsername())) {
            wrapper.like(StudentProcedureExtension::getTeacherUsername, request.getTeacherUsername());
        }
        if (request.getExperimentalProcedureId() != null) {
            wrapper.eq(StudentProcedureExtension::getExperimentalProcedureId, request.getExperimentalProcedureId());
        }

        wrapper.orderByDesc(StudentProcedureExtension::getId);

        List<StudentProcedureExtension> records;
        long total;
        long current = 1;
        long size = 10;

        // 分页查询
        if (Boolean.TRUE.equals(request.getPageable())) {
            Page<StudentProcedureExtension> page = new Page<>(request.getCurrent(), request.getSize());
            Page<StudentProcedureExtension> result = page(page, wrapper);
            records = result.getRecords();
            total = result.getTotal();
            current = result.getCurrent();
            size = result.getSize();
        } else {
            records = list(wrapper);
            total = records.size();
        }

        // 批量查询学生信息填充姓名
        if (!records.isEmpty()) {
            List<String> studentUsernames = records.stream()
                    .map(StudentProcedureExtension::getStudentUsername)
                    .distinct()
                    .collect(Collectors.toList());

            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.in(User::getUsername, studentUsernames);
            List<User> students = userMapper.selectList(userQuery);

            Map<String, String> studentNameMap = students.stream()
                    .collect(Collectors.toMap(User::getUsername, User::getName, (a, b) -> a));

            // 填充学生姓名
            for (StudentProcedureExtension record : records) {
                record.setStudentName(studentNameMap.get(record.getStudentUsername()));
            }
        }

        return PageResponse.of(current, size, total, records);
    }

    /**
     * 按实验ID批量延长（延长该实验下所有步骤）
     *
     * @param experimentId       实验ID
     * @param studentUsernames   学生用户名列表
     * @param extendedMinutes    延长时间（分钟）
     * @param teacherUsername    开通教师用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchExtendByExperiment(
            Long experimentId,
            List<String> studentUsernames,
            Integer extendedMinutes,
            String teacherUsername) {

        // 1. 查询该实验下所有步骤ID
        List<Long> procedureIds = experimentalProcedureService.getProcedureIdsByExperimentId(experimentId);
        if (procedureIds.isEmpty()) {
            throw new BusinessException(404, "该实验没有步骤");
        }

        // 2. 对每个步骤调用批量延长
        for (Long procedureId : procedureIds) {
            batchExtend(procedureId, studentUsernames, extendedMinutes, teacherUsername);
        }

        log.info("教师 {} 为 {} 名学生延长实验 {} 所有 {} 个步骤 {} 分钟",
                teacherUsername, studentUsernames.size(), experimentId, procedureIds.size(), extendedMinutes);
    }
}
