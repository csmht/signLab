package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.enums.ProcedureAccessDeniedReason;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentExperimentalProcedureMapper;
import com.example.demo.pojo.entity.ClassExperimentProcedureTime;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实验步骤答案服务
 * 提供学生实验步骤的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentExperimentalProcedureService extends ServiceImpl<StudentExperimentalProcedureMapper, StudentExperimentalProcedure> {

    private final ExperimentalProcedureService experimentalProcedureService;
    private final ClassExperimentProcedureTimeService classExperimentProcedureTimeService;

    /**
     * 查询学生在指定班级实验中的所有步骤答案
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param experimentId    实验ID
     * @return 步骤答案列表
     */
    public List<StudentExperimentalProcedure> getByStudentAndExperiment(
            String studentUsername, String classCode, Long experimentId) {
        QueryWrapper<StudentExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("class_code", classCode)
                .eq("experiment_id", experimentId);
        return list(queryWrapper);
    }

    /**
     * 查询学生在指定步骤的答案
     *
     * @param studentUsername         学生用户名
     * @param classCode               班级编号
     * @param experimentalProcedureId 实验步骤ID
     * @return 步骤答案记录
     */
    public StudentExperimentalProcedure getByStudentAndProcedure(
            String studentUsername, String classCode, Long experimentalProcedureId) {
        QueryWrapper<StudentExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("class_code", classCode)
                .eq("experimental_procedure_id", experimentalProcedureId);
        return getOne(queryWrapper);
    }

    /**
     * 判断学生是否完成指定步骤
     *
     * @param studentUsername         学生用户名
     * @param classCode               班级编号
     * @param experimentalProcedureId 实验步骤ID
     * @return 是否已完成
     */
    public boolean isProcedureCompleted(String studentUsername, String classCode, Long experimentalProcedureId) {
        StudentExperimentalProcedure studentProcedure = getByStudentAndProcedure(
                studentUsername, classCode, experimentalProcedureId);

        if (studentProcedure == null) {
            return false;
        }

        // 类型1（观看视频）：answer 字段为 "VIEWED"
        // 类型2和3：answer 字段不为空
        return studentProcedure.getAnswer() != null && !studentProcedure.getAnswer().trim().isEmpty();
    }

    /**
     * 判断步骤是否可访问
     * 综合检查前置步骤完成状态、时间窗口、是否可跳过
     *
     * @param experimentId      实验ID
     * @param classCode         班级编号
     * @param studentUsername   学生用户名
     * @param classExperimentId 班级实验ID
     * @param currentProcedure  当前步骤
     * @return 可访问性原因
     */
    public ProcedureAccessDeniedReason checkProcedureAccessible(
            Long experimentId,
            String classCode,
            String studentUsername,
            Long classExperimentId,
            ExperimentalProcedure currentProcedure) {

        LocalDateTime now = LocalDateTime.now();

        // 1. 从 ClassExperimentProcedureTime 表查询时间配置
        ClassExperimentProcedureTime procedureTime = classExperimentProcedureTimeService
                .getByClassExperimentAndProcedure(classExperimentId, currentProcedure.getId());

        if (procedureTime == null) {
            // 如果未配置时间，抛出异常
            throw new BusinessException(500, "步骤时间配置不存在，请联系教师");
        }

        LocalDateTime startTime = procedureTime.getStartTime();
        LocalDateTime endTime = procedureTime.getEndTime();

        // 2. 检查时间窗口
        if (now.isBefore(startTime)) {
            return ProcedureAccessDeniedReason.NOT_STARTED;
        }

        if (now.isAfter(endTime)) {
            return ProcedureAccessDeniedReason.EXPIRED;
        }

        // 3. 如果是第一个步骤，且时间窗口满足，则可访问
        if (currentProcedure.getNumber() == 1) {
            return ProcedureAccessDeniedReason.ACCESSIBLE;
        }

        // 4. 检查是否可跳过
        if (Boolean.TRUE.equals(currentProcedure.getIsSkip())) {
            return ProcedureAccessDeniedReason.ACCESSIBLE;
        }

        // 5. 查询所有步骤，按序号排序
        List<ExperimentalProcedure> allProcedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 6. 检查前置步骤是否完成或已过期
        for (ExperimentalProcedure procedure : allProcedures) {
            // 只需要检查序号小于当前步骤的步骤
            if (procedure.getNumber() >= currentProcedure.getNumber()) {
                break;
            }

            // 如果前置步骤不可跳过，则必须完成或已过期
            if (!Boolean.TRUE.equals(procedure.getIsSkip())) {
                boolean isCompleted = isProcedureCompleted(
                        studentUsername, classCode, procedure.getId());

                // 如果前置步骤未完成，检查是否已过期
                if (!isCompleted) {
                    // 查询该前置步骤的时间配置
                    ClassExperimentProcedureTime prevProcedureTime = classExperimentProcedureTimeService
                            .getByClassExperimentAndProcedure(classExperimentId, procedure.getId());

                    if (prevProcedureTime == null) {
                        throw new BusinessException(500, "前置步骤时间配置不存在，请联系教师");
                    }

                    boolean isExpired = now.isAfter(prevProcedureTime.getEndTime());
                    if (!isExpired) {
                        return ProcedureAccessDeniedReason.PREVIOUS_NOT_COMPLETED;
                    }
                }
            }
        }

        return ProcedureAccessDeniedReason.ACCESSIBLE;
    }

    /**
     * 标记视频已观看
     * 在 StudentExperimentalProcedure 表中插入记录，answer 字段为 "VIEWED"
     *
     * @param studentUsername         学生用户名
     * @param classCode               班级编号
     * @param experimentalProcedureId 实验步骤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markVideoAsViewed(String studentUsername, String classCode, Long experimentalProcedureId) {
        // 1. 查询实验步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(experimentalProcedureId);
        if (procedure == null) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        // 2. 验证步骤类型是否为观看视频（类型1）
        if (!Integer.valueOf(1).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是观看视频类型");
        }

        // 3. 检查是否已标记
        StudentExperimentalProcedure existing = getByStudentAndProcedure(
                studentUsername, classCode, experimentalProcedureId);
        if (existing != null) {
            throw new BusinessException(400, "已标记该视频为已观看");
        }

        // 4. 插入观看记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(experimentalProcedureId);
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer("VIEWED");
        studentProcedure.setCreatedTime(LocalDateTime.now());

        boolean saved = save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "标记视频观看失败");
        }

        log.info("学生 {} 在班级 {} 标记视频 {} 已观看", studentUsername, classCode, experimentalProcedureId);
    }
}
