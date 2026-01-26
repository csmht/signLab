package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.dto.BatchAddUserRequest;
import com.example.demo.pojo.excel.UserImportExcel;
import com.example.demo.service.AuthService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户导入监听器
 * 用于读取Excel数据并进行批量导入
 */
@Slf4j
public class UserImportListener extends AnalysisEventListener<UserImportExcel> {

    /**
     * 批量插入的批次大小
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 临时存储读取到的数据
     */
    private final List<BatchAddUserRequest> userList = new ArrayList<>();

    /**
     * 认证服务
     */
    private final AuthService authService;

    /**
     * 导入结果统计
     */
    private int successCount = 0;
    private int duplicateCount = 0;
    private int failCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    public UserImportListener(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void invoke(UserImportExcel data, AnalysisContext context) {
        try {
            // 跳过空行
            if (data == null || (data.getUsername() == null && data.getName() == null)) {
                return;
            }

            // 数据校验
            validateData(data);

            // 转换为BatchAddUserRequest
            BatchAddUserRequest user = convertToRequest(data);
            userList.add(user);

            // 达到批次大小后，执行批量插入
            if (userList.size() >= BATCH_SIZE) {
                saveData();
            }
        } catch (Exception e) {
            failCount++;
            String errorMsg = String.format("第%d行数据错误: %s", context.readRowHolder().getRowIndex() + 1, e.getMessage());
            errorMessages.add(errorMsg);
            log.error(errorMsg, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余的数据
        if (!userList.isEmpty()) {
            saveData();
        }
        log.info("Excel数据读取完成，成功：{}条，重复：{}条，失败：{}条", successCount, duplicateCount, failCount);
    }

    /**
     * 数据校验
     */
    private void validateData(UserImportExcel data) {
        if (data.getUsername() == null || data.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (data.getRole() == null || data.getRole().trim().isEmpty()) {
            throw new BusinessException(400, "角色不能为空");
        }
        // 验证角色是否有效（支持中文和英文）
        String role = data.getRole().trim();
        if (!isValidRole(role)) {
            throw new BusinessException(400, "角色必须是 学生/教师/管理员 或 student/teacher/admin");
        }
    }

    /**
     * 验证角色是否有效
     */
    private boolean isValidRole(String role) {
        return "student".equalsIgnoreCase(role) || "teacher".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role)
                || "学生".equals(role) || "教师".equals(role) || "管理员".equals(role);
    }

    /**
     * 转换为BatchAddUserRequest
     */
    private BatchAddUserRequest convertToRequest(UserImportExcel data) {
        BatchAddUserRequest request = new BatchAddUserRequest();
        request.setUsername(data.getUsername().trim());
        request.setName(data.getName().trim());
        // 将中文角色转换为英文
        request.setRole(convertRoleToEnglish(data.getRole()));
        request.setDepartment(data.getDepartment() != null ? data.getDepartment().trim() : null);
        request.setMajor(data.getMajor() != null ? data.getMajor().trim() : null);
        return request;
    }

    /**
     * 将中文角色转换为英文
     */
    private String convertRoleToEnglish(String role) {
        if (role == null) {
            return null;
        }
        String trimmedRole = role.trim();
        switch (trimmedRole) {
            case "学生":
                return "student";
            case "教师":
                return "teacher";
            case "管理员":
                return "admin";
            default:
                return trimmedRole.toLowerCase();
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (userList.isEmpty()) {
            return;
        }
        try {
            var response = authService.batchAddUsers(userList);
            successCount += response.getSuccessCount();
            duplicateCount += response.getDuplicateCount();
            failCount += response.getFailCount();
            userList.clear();
        } catch (Exception e) {
            log.error("批量保存用户失败", e);
            failCount += userList.size();
            errorMessages.add("批量保存失败: " + e.getMessage());
            userList.clear();
        }
    }

    /**
     * 获取导入结果
     */
    public ImportResult getResult() {
        return new ImportResult(successCount, duplicateCount, failCount, errorMessages);
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private final int successCount;
        private final int duplicateCount;
        private final int failCount;
        private final List<String> errorMessages;

        public ImportResult(int successCount, int duplicateCount, int failCount, List<String> errorMessages) {
            this.successCount = successCount;
            this.duplicateCount = duplicateCount;
            this.failCount = failCount;
            this.errorMessages = errorMessages;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getDuplicateCount() {
            return duplicateCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }
}