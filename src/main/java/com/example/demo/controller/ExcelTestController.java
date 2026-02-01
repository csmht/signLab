package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.listener.StudentClassImportListener;
import com.example.demo.listener.UserImportListener;
import com.example.demo.pojo.excel.StudentClassImportExcel;
import com.example.demo.pojo.excel.UserImportExcel;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.StudentClassImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel测试导入控制器
 * 提供Excel批量导入数据的测试接口
 */
@RequestMapping("/api/test/excel")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExcelTestController {

    private final AuthService authService;
    private final StudentClassImportService studentClassImportService;

    /**
     * 批量导入用户
     * 通过上传 Excel 文件批量导入用户
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import/users")
    @RequireRole(value = UserRole.ADMIN)
    public ApiResponse<String> importUsers(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ApiResponse.error(400, "请上传Excel文件");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
                return ApiResponse.error(400, "文件格式错误，请上传 .xls 或 .xlsx 格式的Excel文件");
            }

            // 创建监听器
            UserImportListener listener = new UserImportListener(authService);

            // 使用EasyExcel读取Excel文件
            EasyExcel.read(file.getInputStream(), UserImportExcel.class, listener)
                    .sheet()
                    .doRead();

            // 获取导入结果
            UserImportListener.ImportResult result = listener.getResult();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 构建返回消息
            StringBuilder message = new StringBuilder();
            message.append(String.format("导入完成！成功：%d条，重复：%d条，失败：%d条，耗时：%dms",
                    result.getSuccessCount(), result.getDuplicateCount(),
                    result.getFailCount(), duration));

            // 如果有错误信息，添加到返回消息中
            if (!result.getErrorMessages().isEmpty()) {
                message.append("\n错误详情：\n");
                result.getErrorMessages().forEach(error -> message.append(error).append("\n"));
            }

            return ApiResponse.success(message.toString());

        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量添加用户失败", e);
            return ApiResponse.error(500, "批量添加失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户导入模板
     *
     * @return Excel 文件
     */
    @GetMapping("/template/users")
    @RequireRole(value = UserRole.ADMIN)
    public void getUserImportTemplate(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<UserImportExcel> data = new ArrayList<>();

            // 学生示例1
            UserImportExcel student1 = new UserImportExcel();
            student1.setUsername("2021001");
            student1.setName("张三");
            student1.setRole("学生");
            student1.setDepartment("计算机学院");
            student1.setMajor("计算机科学与技术");
            data.add(student1);

            // 学生示例2
            UserImportExcel student2 = new UserImportExcel();
            student2.setUsername("2021002");
            student2.setName("李四");
            student2.setRole("学生");
            student2.setDepartment("计算机学院");
            student2.setMajor("软件工程");
            data.add(student2);

            // 教师示例
            UserImportExcel teacher = new UserImportExcel();
            teacher.setUsername("T001");
            teacher.setName("王老师");
            teacher.setRole("教师");
            teacher.setDepartment("计算机学院");
            data.add(teacher);

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), UserImportExcel.class)
                    .sheet("用户导入模板")
                    .doWrite(data);

        } catch (Exception e) {
            log.error("获取用户导入模板失败", e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"获取模板失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 批量导入学生及班级信息
     * 通过上传 Excel 文件一次性导入学生和对应的班级信息
     * 导入顺序：1.导入学生 -> 2.导入班级 -> 3.绑定关系
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import/students-with-classes")
    @RequireRole(value = UserRole.ADMIN)
    public ApiResponse<String> importStudentsWithClasses(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ApiResponse.error(400, "请上传Excel文件");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
                return ApiResponse.error(400, "文件格式错误，请上传 .xls 或 .xlsx 格式的Excel文件");
            }

            // 创建监听器
            StudentClassImportListener listener = new StudentClassImportListener(studentClassImportService);

            // 使用EasyExcel读取Excel文件
            EasyExcel.read(file.getInputStream(), StudentClassImportExcel.class, listener)
                    .sheet()
                    .doRead();

            // 获取导入结果
            StudentClassImportListener.ImportResult result = listener.getResult();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 构建返回消息
            StringBuilder message = new StringBuilder();
            message.append("导入完成！耗时:").append(duration).append("ms\n");
            message.append("【学生】成功:").append(result.getStudentSuccessCount())
                   .append(" 重复:").append(result.getStudentDuplicateCount())
                   .append(" 失败:").append(result.getStudentFailCount()).append("\n");
            message.append("【班级】成功:").append(result.getClassSuccessCount())
                   .append(" 重复:").append(result.getClassDuplicateCount())
                   .append(" 失败:").append(result.getClassFailCount()).append("\n");
            message.append("【绑定】成功:").append(result.getBindSuccessCount())
                   .append(" 失败:").append(result.getBindFailCount());

            // 如果有错误信息，添加到返回消息中
            if (!result.getErrorMessages().isEmpty()) {
                message.append("\n\n错误详情：\n");
                result.getErrorMessages().forEach(error -> message.append(error).append("\n"));
            }

            return ApiResponse.success(message.toString());

        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量导入学生和班级失败", e);
            return ApiResponse.error(500, "批量导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生班级导入模板
     * 班级编号由系统自动生成，无需在Excel中填写
     *
     * @return Excel 文件
     */
    @GetMapping("/template/students-with-classes")
    @RequireRole(value = UserRole.ADMIN)
    public void getStudentClassImportTemplate(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("学生班级导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<StudentClassImportExcel> data = new ArrayList<>();

            // 示例1：第一个班级
            StudentClassImportExcel example1 = new StudentClassImportExcel();
            example1.setUsername("2021001");
            example1.setName("张三");
            example1.setClassName("计算机科学与技术1班");
            example1.setDepartment("计算机学院");
            example1.setMajor("计算机科学与技术");
            data.add(example1);

            // 示例2：同一班级
            StudentClassImportExcel example2 = new StudentClassImportExcel();
            example2.setUsername("2021002");
            example2.setName("李四");
            example2.setClassName("计算机科学与技术1班"); // 与示例1同班
            example2.setDepartment("计算机学院");
            example2.setMajor("计算机科学与技术");
            data.add(example2);

            // 示例3：不同班级
            StudentClassImportExcel example3 = new StudentClassImportExcel();
            example3.setUsername("2021003");
            example3.setName("王五");
            example3.setClassName("软件工程1班"); // 不同班级
            example3.setDepartment("计算机学院");
            example3.setMajor("软件工程");
            data.add(example3);

            // 示例4：同班级的第三个学生
            StudentClassImportExcel example4 = new StudentClassImportExcel();
            example4.setUsername("2021004");
            example4.setName("赵六");
            example4.setClassName("计算机科学与技术1班"); // 与示例1、2同班
            example4.setDepartment("计算机学院");
            example4.setMajor("计算机科学与技术");
            data.add(example4);

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), StudentClassImportExcel.class)
                    .sheet("学生班级导入模板")
                    .doWrite(data);

        } catch (Exception e) {
            log.error("获取学生班级导入模板失败", e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"获取模板失败: " + e.getMessage() + "\"}");
        }
    }
}