package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.dto.BatchAddUserRequest;
import com.example.demo.pojo.dto.BatchAddUserResponse;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel测试导入控制器
 * 提供Excel批量导入数据的测试接口
 * 文件必须放在项目根目录下
 */
@RequestMapping("/api/test/excel")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExcelTestController {

    private final AuthService authService;

    /**
     * 批量导入用户
     * 文件必须放在项目根目录下，文件名为 users.xls 或 users.xlsx
     *
     * @return 导入结果
     */
    @PostMapping("/import/users")
    @RequireRole(value = UserRole.ADMIN)
    public ApiResponse<String> importUsers() {
        long startTime = System.currentTimeMillis();
        try {
            // 项目根目录
            String projectRoot = System.getProperty("user.dir");
            java.io.File excelFile = new java.io.File(projectRoot, "users.xls");
            if (!excelFile.exists()) {
                excelFile = new java.io.File(projectRoot, "users.xlsx");
            }

            if (!excelFile.exists()) {
                return ApiResponse.error(400, "文件不存在，请将 users.xls 或 users.xlsx 文件放在项目根目录下");
            }

            // 读取Excel文件，按列序号映射
            List<Object> rawData = com.alibaba.excel.EasyExcel.read(excelFile)
                    .sheet()
                    .doReadSync();

            if (rawData == null || rawData.isEmpty()) {
                return ApiResponse.error(400, "Excel文件中没有数据");
            }

            // 跳过标题行，从第二行开始
            List<BatchAddUserRequest> users = new ArrayList<>();
            for (int i = 1; i < rawData.size(); i++) {
                Object row = rawData.get(i);
                if (row instanceof List) {
                    List<?> rowData = (List<?>) row;

                    // 按列序号映射：id(0), username(1), name(2), password(3), role(4)
                    if (rowData.size() > 4) {
                        BatchAddUserRequest user = new BatchAddUserRequest();
                        user.setUsername(getStringValue(rowData, 1)); // 第2列
                        user.setName(getStringValue(rowData, 2));      // 第3列
                        user.setRole(getStringValue(rowData, 4));     // 第5列

                        // 院系（第10列，索引9）
                        if (rowData.size() > 9) {
                            user.setDepartment(getStringValue(rowData, 9));
                        }

                        // 专业（第11列，索引10）
                        if (rowData.size() > 10) {
                            user.setMajor(getStringValue(rowData, 10));
                        }

                        users.add(user);
                    }
                }
            }

            if (users.isEmpty()) {
                return ApiResponse.error(400, "Excel文件中没有有效数据");
            }

            BatchAddUserResponse response = authService.batchAddUsers(users);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            String message = String.format("导入完成！成功：%d条，重复：%d条，失败：%d条，耗时：%dms",
                    response.getSuccessCount(), response.getDuplicateCount(),
                    response.getFailCount(), duration);

            return ApiResponse.success(message);

        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量添加用户失败", e);
            return ApiResponse.error(500, "批量添加失败: " + e.getMessage());
        }
    }

    /**
     * 从列表中获取字符串值
     *
     * @param list 列表
     * @param index 索引
     * @return 字符串值
     */
    private String getStringValue(List<?> list, int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        return value == null ? null : value.toString().trim();
    }
}