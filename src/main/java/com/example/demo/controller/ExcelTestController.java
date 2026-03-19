package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.listener.ClassCourseExperimentExcelListener;
import com.example.demo.listener.StudentClassImportListener;
import com.example.demo.listener.UserImportListener;
import com.example.demo.pojo.excel.ClassCourseExperimentExcel;
import com.example.demo.pojo.excel.StudentClassImportExcel;
import com.example.demo.pojo.excel.UserImportExcel;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.ClassCourseExperimentImportService;
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
@RequestMapping("/api/excel")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExcelTestController {

    private final AuthService authService;
    private final StudentClassImportService studentClassImportService;
    private final ClassCourseExperimentImportService classCourseExperimentImportService;

    /**
     * 批量导入用户
     * 通过上传 Excel 文件批量导入用户
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import/users")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<String> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
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

        // 判断返回码：全部成功返回200，部分成功返回201，全部失败返回400
        int totalProcessed = result.getSuccessCount() + result.getDuplicateCount() + result.getFailCount();
        if (result.getSuccessCount() > 0 && result.getFailCount() > 0) {
            return new ApiResponse<String>(201, message.toString(), null);
        } else if (result.getSuccessCount() == 0 && totalProcessed > 0) {
            return ApiResponse.error(400, message.toString());
        } else {
            return ApiResponse.success(message.toString());
        }
    }

    /**
     * 获取用户导入模板
     *
     * @return Excel 文件
     */
    @GetMapping("/template/users")
    @RequireRole(value = UserRole.TEACHER)
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
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<String> importStudentsWithClasses(@RequestParam("file") MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
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

        // 判断返回码：全部成功返回200，部分成功返回201，全部失败返回400
        int totalFailures = result.getStudentFailCount() + result.getClassFailCount() + result.getBindFailCount();
        int totalSuccess = result.getStudentSuccessCount() + result.getClassSuccessCount() + result.getBindSuccessCount();
        if (totalSuccess > 0 && totalFailures > 0) {
            return new ApiResponse<String>(201, message.toString(), null);
        } else if (totalSuccess == 0 && totalFailures > 0) {
            return ApiResponse.error(400, message.toString());
        } else {
            return ApiResponse.success(message.toString());
        }
    }

    /**
     * 获取学生班级导入模板
     * 班级编号由系统自动生成，无需在Excel中填写
     *
     * @return Excel 文件
     */
    @GetMapping("/template/students-with-classes")
    @RequireRole(value = UserRole.TEACHER)
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

    /**
     * 批量导入班级课程实验
     * 通过上传 Excel 文件一次性导入班级、课程、实验、课次信息
     * 支持合班上课（同一时间多个班级）
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import/class-course-experiments")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<String> importClassCourseExperiments(@RequestParam("file") MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        // 验证文件
        if (file == null || file.isEmpty()) {
            return ApiResponse.error(400, "请上传Excel文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
            return ApiResponse.error(400, "文件格式错误，请上传 .xls 或 .xlsx 格式的Excel文件");
        }

        // 创建监听器
        ClassCourseExperimentExcelListener listener = new ClassCourseExperimentExcelListener(classCourseExperimentImportService);

        // 使用EasyExcel读取Excel文件
        EasyExcel.read(file.getInputStream(), ClassCourseExperimentExcel.class, listener)
                .sheet()
                .doRead();

        // 获取导入结果
        ClassCourseExperimentExcelListener.ImportResult result = listener.getResult();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 构建返回消息
        StringBuilder message = new StringBuilder();
        message.append("导入完成！耗时:").append(duration).append("ms\n");
        message.append("【班级】成功:").append(result.getClassSuccessCount())
               .append(" 重复:").append(result.getClassDuplicateCount())
               .append(" 失败:").append(result.getClassFailCount()).append("\n");
        message.append("【课程】成功:").append(result.getCourseSuccessCount())
               .append(" 重复:").append(result.getCourseDuplicateCount())
               .append(" 失败:").append(result.getCourseFailCount()).append("\n");
        message.append("【实验】成功:").append(result.getExperimentSuccessCount())
               .append(" 重复:").append(result.getExperimentDuplicateCount())
               .append(" 失败:").append(result.getExperimentFailCount()).append("\n");
        message.append("【课次】成功:").append(result.getClassExperimentSuccessCount())
               .append(" 重复:").append(result.getClassExperimentDuplicateCount())
               .append(" 失败:").append(result.getClassExperimentFailCount());

        // 如果有错误信息，添加到返回消息中
        if (!result.getErrorMessages().isEmpty()) {
            message.append("\n\n错误详情：\n");
            result.getErrorMessages().forEach(error -> message.append(error).append("\n"));
        }

        // 判断返回码：全部成功返回200，有失败则抛出异常返回500
        int totalFailures = result.getClassFailCount() + result.getCourseFailCount()
                + result.getExperimentFailCount() + result.getClassExperimentFailCount();
        if (totalFailures > 0) {
            throw new com.example.demo.exception.BusinessException(500, message.toString());
        }
        return ApiResponse.success(message.toString());
    }

    /**
     * 获取班级课程实验导入模板
     *
     * @return Excel 文件
     */
    @GetMapping("/template/class-course-experiments")
    @RequireRole(value = UserRole.TEACHER)
    public void getClassCourseExperimentImportTemplate(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("班级课程实验导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<ClassCourseExperimentExcel> data = new ArrayList<>();

            // 示例1：单班上课
            ClassCourseExperimentExcel example1 = new ClassCourseExperimentExcel();
            example1.setClassName("计算机科学与技术1班");
            example1.setCourseName("数据结构");
            example1.setExperimentName("链表操作实验");
            example1.setData("2024-03-15");
            example1.setTime("8:00-10:00");
            example1.setLaboratory("实验楼A");
            example1.setLaboratory2("101");
            example1.setTeacherName("张老师");
            data.add(example1);

            // 示例2：同一课程不同实验
            ClassCourseExperimentExcel example2 = new ClassCourseExperimentExcel();
            example2.setClassName("计算机科学与技术1班");
            example2.setCourseName("数据结构");
            example2.setExperimentName("树遍历实验");
            example2.setData("2024-03-22");
            example2.setTime("14:00-16:00");
            example2.setLaboratory("实验楼A");
            example2.setLaboratory2("102");
            example2.setTeacherName("张老师");
            data.add(example2);

            // 示例3：合班上课（与示例4同时段）
            ClassCourseExperimentExcel example3 = new ClassCourseExperimentExcel();
            example3.setClassName("软件工程1班");
            example3.setCourseName("操作系统");
            example3.setExperimentName("进程调度实验");
            example3.setData("2024-03-20");
            example3.setTime("10:00-12:00");
            example3.setLaboratory("实验楼B");
            example3.setLaboratory2("201");
            example3.setTeacherName("李老师");
            data.add(example3);

            // 示例4：合班上课（与示例3同时段）
            ClassCourseExperimentExcel example4 = new ClassCourseExperimentExcel();
            example4.setClassName("软件工程2班");
            example4.setCourseName("操作系统");
            example4.setExperimentName("进程调度实验");
            example4.setData("2024-03-20");
            example4.setTime("10:00-12:00");
            example4.setLaboratory("实验楼B");
            example4.setLaboratory2("201");
            example4.setTeacherName("李老师");
            data.add(example4);

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), ClassCourseExperimentExcel.class)
                    .sheet("班级课程实验导入模板")
                    .doWrite(data);

        } catch (Exception e) {
            log.error("获取班级课程实验导入模板失败", e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"获取模板失败: " + e.getMessage() + "\"}");
        }
    }
}
