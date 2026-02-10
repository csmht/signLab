package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.request.BatchBindStudentsRequest;
import com.example.demo.pojo.request.StudentQueryRequest;
import com.example.demo.pojo.response.BatchBindStudentsResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 学生班级关系服务
 * 提供学生班级关系的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentClassRelationService extends ServiceImpl<StudentClassRelationMapper, StudentClassRelation> {

    private final UserMapper userMapper;
    private final ClassMapper classMapper;

    /**
     * 根据学生用户名查询班级关系列表
     */
    public List<StudentClassRelation> getByStudentUsername(String studentUsername) {
        LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentClassRelation::getStudentUsername, studentUsername);
        return list(queryWrapper);
    }

    /**
     * 根据班级代码查询学生列表
     */
    public List<StudentClassRelation> getByClassCode(String classCode) {
        LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentClassRelation::getClassCode, classCode);
        return list(queryWrapper);
    }

    /**
     * 根据班级代码分页查询学生列表
     *
     * @param classCode 班级代码
     * @param request   查询请求
     * @return 分页结果
     */
    public PageResponse<StudentClassRelation> getStudentsByClassCodePage(
            String classCode, StudentQueryRequest request) {

        LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentClassRelation::getClassCode, classCode);

        // 支持按学生用户名模糊查询
        if (request.getStudentUsername() != null && !request.getStudentUsername().trim().isEmpty()) {
            queryWrapper.like(StudentClassRelation::getStudentUsername, request.getStudentUsername().trim());
        }

        // 按绑定时间倒序排序
        queryWrapper.orderByDesc(StudentClassRelation::getBindTime);

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<StudentClassRelation> page = new Page<>(request.getCurrent(), request.getSize());
            Page<StudentClassRelation> result = page(page, queryWrapper);
            return PageResponse.of(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    result.getRecords()
            );
        } else {
            // 不分页，返回全部数据
            List<StudentClassRelation> records = list(queryWrapper);
            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 批量绑定学生到班级
     *
     * @param request 批量绑定学生请求
     * @return 批量绑定学生响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchBindStudentsResponse batchBindStudents(BatchBindStudentsRequest request) {
        BatchBindStudentsResponse response = new BatchBindStudentsResponse();
        response.setClassCode(request.getClassCode());
        response.setSuccessCount(0);
        response.setFailCount(0);
        response.setSuccessList(new ArrayList<>());
        response.setFailList(new ArrayList<>());

        // 查询班级信息
        LambdaQueryWrapper<Class> classQuery = new LambdaQueryWrapper<>();
        classQuery.eq(Class::getClassCode, request.getClassCode());
        com.example.demo.pojo.entity.Class clazz = classMapper.selectOne(classQuery);
        if (clazz == null) {
            throw new BusinessException(404, "班级不存在");
        }
        response.setClassName(clazz.getClassName());

        if (request.getStudentUsernames() == null || request.getStudentUsernames().isEmpty()) {
            throw new BusinessException(400, "学生列表不能为空");
        }

        for (String studentUsername : request.getStudentUsernames()) {
            BatchBindStudentsResponse.StudentResult result = new BatchBindStudentsResponse.StudentResult();
            result.setStudentUsername(studentUsername);

            try {
                // 检查学生是否存在
                User student = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUsername, studentUsername)
                );
                if (student == null) {
                    result.setSuccess(false);
                    result.setMessage("学生不存在");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }
                result.setStudentName(student.getName());

                // 检查学生是否已绑定到该班级
                LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StudentClassRelation::getStudentUsername, studentUsername)
                        .eq(StudentClassRelation::getClassCode, request.getClassCode());
                StudentClassRelation existingRelation = getOne(queryWrapper);
                if (existingRelation != null) {
                    result.setSuccess(false);
                    result.setMessage("学生已绑定到该班级");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }

                // 创建绑定关系
                StudentClassRelation relation = new StudentClassRelation();
                relation.setStudentUsername(studentUsername);
                relation.setClassCode(request.getClassCode());

                boolean saved = save(relation);
                if (saved) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                    response.setSuccessCount(response.getSuccessCount() + 1);

                    log.info("学生 {} 绑定到班级 {} 成功", studentUsername, request.getClassCode());
                } else {
                    result.setSuccess(false);
                    result.setMessage("绑定失败");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                }
            } catch (Exception e) {
                log.error("绑定学生 {} 到班级 {} 失败", studentUsername, request.getClassCode(), e);
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                response.getFailList().add(result);
                response.setFailCount(response.getFailCount() + 1);
            }
        }

        return response;
    }

    /**
     * 批量解绑学生
     *
     * @param classCode 班级编号
     * @param studentUsernames 学生用户名列表
     * @return 解绑的学生数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUnbindStudents(String classCode, List<String> studentUsernames) {
        if (studentUsernames == null || studentUsernames.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String studentUsername : studentUsernames) {
            LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentClassRelation::getStudentUsername, studentUsername)
                    .eq(StudentClassRelation::getClassCode, classCode);
            boolean removed = remove(queryWrapper);
            if (removed) {
                count++;
                log.info("学生 {} 从班级 {} 解绑成功", studentUsername, classCode);
            }
        }

        return count;
    }

    /**
     * 学生通过验证码绑定班级
     *
     * @param studentUsername 学生用户名
     * @param verificationCode 班级验证码
     * @return 绑定的班级信息
     */
    @Transactional(rollbackFor = Exception.class)
    public com.example.demo.pojo.entity.Class bindClass(String studentUsername, String verificationCode) {
        // 验证学生是否存在
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, studentUsername)
        );
        if (student == null) {
            throw new BusinessException(400, "学生不存在");
        }

        // 根据验证码查询班级
        LambdaQueryWrapper<com.example.demo.pojo.entity.Class> classQuery = new LambdaQueryWrapper<>();
        classQuery.apply("verification_code = {0}", verificationCode);
        com.example.demo.pojo.entity.Class clazz = classMapper.selectOne(classQuery);
        if (clazz == null) {
            throw new BusinessException(400, "班级验证码无效");
        }

        // 检查是否已绑定过该班级
        LambdaQueryWrapper<StudentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentClassRelation::getStudentUsername, studentUsername)
                .eq(StudentClassRelation::getClassCode, clazz.getClassCode());
        StudentClassRelation existingRelation = getOne(queryWrapper);
        if (existingRelation != null) {
            throw new BusinessException(400, "已绑定过该班级");
        }

        // 创建绑定关系
        StudentClassRelation relation = new StudentClassRelation();
        relation.setStudentUsername(studentUsername);
        relation.setClassCode(clazz.getClassCode());

        boolean saved = save(relation);
        if (!saved) {
            throw new BusinessException(500, "绑定班级失败");
        }

        log.info("学生 {} 通过验证码绑定到班级 {} 成功", studentUsername, clazz.getClassCode());

        return clazz;
    }

    /**
     * 查询学生的班级列表（包含班级详细信息）
     *
     * @param studentUsername 学生用户名
     * @return 班级信息列表
     */
    public java.util.List<com.example.demo.pojo.response.ClassInfoResponse> getStudentClassInfoList(String studentUsername) {
        // 查询学生的班级关系
        java.util.List<StudentClassRelation> relations = getByStudentUsername(studentUsername);

        // 转换为班级信息响应
        return relations.stream().map(relation -> {
            com.example.demo.pojo.response.ClassInfoResponse response =
                    new com.example.demo.pojo.response.ClassInfoResponse();
            response.setClassCode(relation.getClassCode());
            response.setBindTime(relation.getBindTime());

            // 查询班级详细信息
            // 使用StudentClassRelationMapper查询学生数量
            int studentCount = baseMapper.countStudentsByClassCode(relation.getClassCode());
            response.setStudentCount(studentCount);

            return response;
        }).collect(java.util.stream.Collectors.toList());
    }
}