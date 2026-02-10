package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.enums.ProcedureAccessDeniedReason;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentClassRelationMapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.StudentExperimentalProcedureMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.ClassExperimentClassRelation;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.util.ProcedureTimeCalculator;
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
    private final ClassExperimentMapper classExperimentMapper;
    private final ClassExperimentClassRelationMapper classExperimentClassRelationMapper;

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
        LambdaQueryWrapper<StudentExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername)
                .eq(StudentExperimentalProcedure::getClassCode, classCode)
                .eq(StudentExperimentalProcedure::getExperimentId, experimentId);
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
        LambdaQueryWrapper<StudentExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername)
                .eq(StudentExperimentalProcedure::getClassCode, classCode)
                .eq(StudentExperimentalProcedure::getExperimentalProcedureId, experimentalProcedureId);
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

        // 1. 查询班级实验获取实验开始时间
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        // 2. 计算步骤的开始和结束时间
        LocalDateTime startTime = ProcedureTimeCalculator.calculateStartTime(
                classExperiment.getStartTime(),
                currentProcedure.getOffsetMinutes()
        );
        LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                startTime,
                currentProcedure.getDurationMinutes()
        );

        if (startTime == null || endTime == null) {
            throw new BusinessException(500, "步骤时间配置不完整，请联系教师");
        }

        // 3. 检查时间窗口
        if (now.isBefore(startTime)) {
            return ProcedureAccessDeniedReason.NOT_STARTED;
        }

        if (now.isAfter(endTime)) {
            return ProcedureAccessDeniedReason.EXPIRED;
        }

        // 4. 如果是第一个步骤，且时间窗口满足，则可访问
        if (currentProcedure.getNumber() == 1) {
            return ProcedureAccessDeniedReason.ACCESSIBLE;
        }

        // 5. 检查是否可跳过
        if (Boolean.TRUE.equals(currentProcedure.getIsSkip())) {
            return ProcedureAccessDeniedReason.ACCESSIBLE;
        }

        // 6. 查询所有步骤，按序号排序
        List<ExperimentalProcedure> allProcedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 7. 检查前置步骤是否完成或已过期
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
                    // 计算前置步骤的结束时间
                    LocalDateTime prevProcedureStartTime = ProcedureTimeCalculator.calculateStartTime(
                            classExperiment.getStartTime(),
                            procedure.getOffsetMinutes()
                    );
                    LocalDateTime prevProcedureEndTime = ProcedureTimeCalculator.calculateEndTime(
                            prevProcedureStartTime,
                            procedure.getDurationMinutes()
                    );

                    if (prevProcedureEndTime == null) {
                        throw new BusinessException(500, "前置步骤时间配置不完整，请联系教师");
                    }

                    boolean isExpired = now.isAfter(prevProcedureEndTime);
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

        log.info("学生 {} 在班级 {} 标���视频 {} 已观看", studentUsername, classCode, experimentalProcedureId);
    }

    /**
     * 判断学生步骤是否可修改
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param experimentalProcedureId 实验步骤ID
     * @return 是否可修改
     */
    public boolean isProcedureModifiable(String studentUsername, String classCode, Long experimentalProcedureId) {
        // 1. 查询提交记录
        StudentExperimentalProcedure existing = getByStudentAndProcedure(
                studentUsername, classCode, experimentalProcedureId);
        if (existing == null) {
            return false; // 未提交
        }

        // 2. 检查是否已教师评分
        if (existing.getIsGraded() != null && existing.getIsGraded() == 1) {
            return false; // 已教师评分
        }

        // 3. 查询步骤时间配置
        ExperimentalProcedure procedure = experimentalProcedureService.getById(experimentalProcedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            return false;
        }

        // 4. 查询班级实验时间
        LambdaQueryWrapper<ClassExperiment> wrapper = new LambdaQueryWrapper<>();

        List<Long> relationIds;

        {
            LambdaQueryWrapper<ClassExperimentClassRelation> a = new LambdaQueryWrapper<ClassExperimentClassRelation>();
            a.eq(ClassExperimentClassRelation::getClassCode,classCode);
            List<ClassExperimentClassRelation> classExperimentClassRelations = classExperimentClassRelationMapper.selectList(a);
            relationIds = classExperimentClassRelations.stream().map(ClassExperimentClassRelation::getId).toList();
        }
        wrapper.eq(ClassExperiment::getId, relationIds);
        wrapper.eq(ClassExperiment::getExperimentId, procedure.getExperimentId());
        ClassExperiment classExperiment = classExperimentMapper.selectOne(wrapper);

        if (classExperiment == null) {
            return false;
        }

        // 5. 计算步骤结束时间
        LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                ProcedureTimeCalculator.calculateStartTime(
                        classExperiment.getStartTime(),
                        procedure.getOffsetMinutes()
                ),
                procedure.getDurationMinutes()
        );

        // 6. 检查是否在时间窗口内
        if (endTime == null || LocalDateTime.now().isAfter(endTime)) {
            return false; // 已过结束时间
        }

        return true;
    }
}
