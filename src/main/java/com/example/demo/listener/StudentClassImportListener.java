package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.excel.StudentClassImportExcel;
import com.example.demo.pojo.response.BatchImportStudentClassResponse;
import com.example.demo.service.StudentClassImportService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 学���班级导入监听器
 * 用于读取Excel数据并进行批量导入
 */
@Slf4j
public class StudentClassImportListener extends AnalysisEventListener<StudentClassImportExcel> {

    /**
     * 批量插入的批次大小
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 临时存储读取到的数据
     */
    private final List<StudentClassImportExcel> dataList = new ArrayList<>();

    /**
     * 学生班级导入服务
     */
    private final StudentClassImportService importService;

    /**
     * 导入结果统计
     */
    private int studentSuccessCount = 0;
    private int studentDuplicateCount = 0;
    private int studentFailCount = 0;
    private int classSuccessCount = 0;
    private int classDuplicateCount = 0;
    private int classFailCount = 0;
    private int bindSuccessCount = 0;
    private int bindFailCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    public StudentClassImportListener(StudentClassImportService importService) {
        this.importService = importService;
    }

    @Override
    public void invoke(StudentClassImportExcel data, AnalysisContext context) {
        try {
            // 跳过空行
            if (data == null || (data.getUsername() == null && data.getName() == null)) {
                return;
            }

            // 数据校验
            validateData(data, context.readRowHolder().getRowIndex() + 1);

            // 添加到批次列表
            dataList.add(data);

            // 达到批次大小后，执行批量导入
            if (dataList.size() >= BATCH_SIZE) {
                saveData();
            }
        } catch (Exception e) {
            studentFailCount++;
            String errorMsg = String.format("第%d行数据错误: %s",
                    context.readRowHolder().getRowIndex() + 1, e.getMessage());
            errorMessages.add(errorMsg);
            log.error(errorMsg, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余的数据
        if (!dataList.isEmpty()) {
            saveData();
        }
        log.info("Excel数据读取完成，学生 - 成功：{}条，重复：{}条，失败：{}条 | 班级 - 成功：{}条，重复：{}条，失败：{}条 | 绑定 - 成功：{}条，失败：{}条",
                studentSuccessCount, studentDuplicateCount, studentFailCount,
                classSuccessCount, classDuplicateCount, classFailCount,
                bindSuccessCount, bindFailCount);
    }

    /**
     * 数据校验
     *
     * @param data     Excel数据
     * @param rowIndex 行号
     */
    private void validateData(StudentClassImportExcel data, int rowIndex) {
        if (data.getUsername() == null || data.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (data.getClassName() == null || data.getClassName().trim().isEmpty()) {
            throw new BusinessException(400, "班级名称不能为空");
        }
        // 如果提供了班级编号，验证格式
        if (data.getClassCode() != null && !data.getClassCode().trim().isEmpty()) {
            if (!data.getClassCode().matches("CLASS\\d{6}")) {
                throw new BusinessException(400, "班级编号格式错误，应为 CLASS + 6位数字");
            }
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (dataList.isEmpty()) {
            return;
        }

        try {
            // 调用服务层执行导入
            BatchImportStudentClassResponse response =
                    importService.batchImportStudentWithClass(dataList);

            // 更新统计信息
            studentSuccessCount += response.getStudentSuccessCount();
            studentDuplicateCount += response.getStudentDuplicateCount();
            studentFailCount += response.getStudentFailCount();
            classSuccessCount += response.getClassSuccessCount();
            classDuplicateCount += response.getClassDuplicateCount();
            classFailCount += response.getClassFailCount();
            bindSuccessCount += response.getBindSuccessCount();
            bindFailCount += response.getBindFailCount();
            errorMessages.addAll(response.getErrorMessages());

            // 清空批次列表
            dataList.clear();

        } catch (Exception e) {
            log.error("批量导入失败", e);
            studentFailCount += dataList.size();
            errorMessages.add("批量导入失败: " + e.getMessage());
            dataList.clear();
        }
    }

    /**
     * 获取导入结果
     *
     * @return 导入结果
     */
    public ImportResult getResult() {
        return new ImportResult(
                studentSuccessCount, studentDuplicateCount, studentFailCount,
                classSuccessCount, classDuplicateCount, classFailCount,
                bindSuccessCount, bindFailCount,
                errorMessages
        );
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private final int studentSuccessCount;
        private final int studentDuplicateCount;
        private final int studentFailCount;
        private final int classSuccessCount;
        private final int classDuplicateCount;
        private final int classFailCount;
        private final int bindSuccessCount;
        private final int bindFailCount;
        private final List<String> errorMessages;

        public ImportResult(int studentSuccessCount, int studentDuplicateCount, int studentFailCount,
                           int classSuccessCount, int classDuplicateCount, int classFailCount,
                           int bindSuccessCount, int bindFailCount, List<String> errorMessages) {
            this.studentSuccessCount = studentSuccessCount;
            this.studentDuplicateCount = studentDuplicateCount;
            this.studentFailCount = studentFailCount;
            this.classSuccessCount = classSuccessCount;
            this.classDuplicateCount = classDuplicateCount;
            this.classFailCount = classFailCount;
            this.bindSuccessCount = bindSuccessCount;
            this.bindFailCount = bindFailCount;
            this.errorMessages = errorMessages;
        }

        public int getStudentSuccessCount() {
            return studentSuccessCount;
        }

        public int getStudentDuplicateCount() {
            return studentDuplicateCount;
        }

        public int getStudentFailCount() {
            return studentFailCount;
        }

        public int getClassSuccessCount() {
            return classSuccessCount;
        }

        public int getClassDuplicateCount() {
            return classDuplicateCount;
        }

        public int getClassFailCount() {
            return classFailCount;
        }

        public int getBindSuccessCount() {
            return bindSuccessCount;
        }

        public int getBindFailCount() {
            return bindFailCount;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }
}
