package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.dto.BatchBindStudentsRequest;
import com.example.demo.pojo.dto.BatchBindStudentsResponse;
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
    private final ClassService classService;

    /**
     * 根据学生用户名查询班级关系列表
     */
    public List<StudentClassRelation> getByStudentUsername(String studentUsername) {
        QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return list(queryWrapper);
    }

    /**
     * 根据班级代码查询学生列表
     */
    public List<StudentClassRelation> getByClassCode(String classCode) {
        QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return list(queryWrapper);
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
        com.example.demo.pojo.entity.Class clazz = classService.getByClassCode(request.getClassCode());
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
                        new QueryWrapper<User>().eq("username", studentUsername)
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
                QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("student_username", studentUsername)
                        .eq("class_code", request.getClassCode());
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

                    // 更新班级人数
                    classService.updateStudentCount(request.getClassCode(), 1);

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
            QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_username", studentUsername)
                    .eq("class_code", classCode);
            boolean removed = remove(queryWrapper);
            if (removed) {
                count++;
                // 更新班级人数
                classService.updateStudentCount(classCode, -1);
                log.info("学生 {} 从班级 {} 解绑成功", studentUsername, classCode);
            }
        }

        return count;
    }
}