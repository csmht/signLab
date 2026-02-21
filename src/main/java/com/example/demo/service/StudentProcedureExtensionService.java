package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentProcedureExtensionMapper;
import com.example.demo.pojo.entity.StudentProcedureExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生步骤时间延长服务
 * 提供步骤时间延长的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcedureExtensionService extends ServiceImpl<StudentProcedureExtensionMapper, StudentProcedureExtension> {

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
     * 批量设置学生步骤延长时间（优化版：批量查询 + 批量插入/更新）
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

        // 2. 构建已存在记录的 Map（学生用户名 -> 记录）
        Map<String, StudentProcedureExtension> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(
                        StudentProcedureExtension::getStudentUsername,
                        e -> e,
                        (e1, e2) -> e1
                ));

        // 3. 分离需要更新和需要新增的记录
        List<StudentProcedureExtension> toUpdate = new ArrayList<>();
        List<StudentProcedureExtension> toInsert = new ArrayList<>();

        for (String studentUsername : studentUsernames) {
            StudentProcedureExtension existing = existingMap.get(studentUsername);
            if (existing != null) {
                // 更新现有记录
                existing.setExtendedMinutes(extendedMinutes);
                existing.setTeacherUsername(teacherUsername);
                toUpdate.add(existing);
            } else {
                // 创建新记录
                StudentProcedureExtension extension = new StudentProcedureExtension();
                extension.setStudentUsername(studentUsername);
                extension.setExperimentalProcedureId(procedureId);
                extension.setExtendedMinutes(extendedMinutes);
                extension.setTeacherUsername(teacherUsername);
                toInsert.add(extension);
            }
        }

        // 4. 批量更新和插入
        if (!toUpdate.isEmpty()) {
            updateBatchById(toUpdate);
        }
        if (!toInsert.isEmpty()) {
            saveBatch(toInsert);
        }

        log.info("教师 {} 为 {} 名学生设置步骤 {} 延长时间 {} 分钟（更新 {} 条，新增 {} 条）",
                teacherUsername, studentUsernames.size(), procedureId, extendedMinutes,
                toUpdate.size(), toInsert.size());
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
     * 查询步骤的延长记录列表
     *
     * @param procedureId 实验步骤ID
     * @return 延长记录列表
     */
    public List<StudentProcedureExtension> getExtensionsByProcedureId(Long procedureId) {
        LambdaQueryWrapper<StudentProcedureExtension> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProcedureExtension::getExperimentalProcedureId, procedureId);
        return list(wrapper);
    }
}
