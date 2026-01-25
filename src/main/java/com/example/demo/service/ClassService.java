package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.pojo.dto.BatchAddClassRequest;
import com.example.demo.pojo.dto.BatchAddClassResponse;
import com.example.demo.pojo.entity.Class;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务
 * 提供班级的业务逻辑处理
 */
@Slf4j
@Service
public class ClassService extends ServiceImpl<ClassMapper, Class> {

    /**
     * 根据班级代码查询班级
     */
    public Class getByClassCode(String classCode) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }

    /**
     * 批量添加班级
     *
     * @param request 批量添加班级请求
     * @return 批量添加班级响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchAddClassResponse batchAddClasses(BatchAddClassRequest request) {
        BatchAddClassResponse response = new BatchAddClassResponse();
        response.setSuccessCount(0);
        response.setFailCount(0);
        response.setSuccessList(new ArrayList<>());
        response.setFailList(new ArrayList<>());

        if (request.getClasses() == null || request.getClasses().isEmpty()) {
            throw new BusinessException(400, "班级列表不能为空");
        }

        for (BatchAddClassRequest.ClassInfo classInfo : request.getClasses()) {
            BatchAddClassResponse.ClassResult result = new BatchAddClassResponse.ClassResult();
            result.setClassCode(classInfo.getClassCode());
            result.setClassName(classInfo.getClassName());

            try {
                // 检查班级是否已存在
                Class existingClass = getByClassCode(classInfo.getClassCode());
                if (existingClass != null) {
                    result.setSuccess(false);
                    result.setMessage("班级编号已存在");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }

                // 创建班级
                Class clazz = new Class();
                clazz.setClassCode(classInfo.getClassCode());
                clazz.setClassName(classInfo.getClassName());
                clazz.setVerificationCode(classInfo.getVerificationCode());
                clazz.setStudentCount(0);

                boolean saved = save(clazz);
                if (saved) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                    log.info("班级 {} 添加成功", classInfo.getClassCode());
                } else {
                    result.setSuccess(false);
                    result.setMessage("班级添加失败");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                }
            } catch (Exception e) {
                log.error("添加班级 {} 失败", classInfo.getClassCode(), e);
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                response.getFailList().add(result);
                response.setFailCount(response.getFailCount() + 1);
            }
        }

        return response;
    }

    /**
     * 更新班级人数
     *
     * @param classCode 班级编号
     * @param delta 人数变化量
     */
    public void updateStudentCount(String classCode, int delta) {
        Class clazz = getByClassCode(classCode);
        if (clazz != null) {
            int newCount = clazz.getStudentCount() + delta;
            if (newCount < 0) {
                newCount = 0;
            }
            clazz.setStudentCount(newCount);
            updateById(clazz);
        }
    }
}