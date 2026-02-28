package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.excel.ClassCourseExperimentExcel;
import com.example.demo.service.ClassCourseExperimentImportService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 班级课程实验Excel导入监听器
 * 用于读取Excel数据并进行批量导入
 */
@Slf4j
public class ClassCourseExperimentExcelListener extends AnalysisEventListener<ClassCourseExperimentExcel> {

    /**
     * 批量插入的批次大小
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 临时存储读取到的数据
     */
    private final List<ClassCourseExperimentExcel> dataList = new ArrayList<>();

    /**
     * 班级课程实验导入服务
     */
    private final ClassCourseExperimentImportService importService;

    // ========== 统计计数器 ==========
    private int classSuccessCount = 0;
    private int classDuplicateCount = 0;
    private int classFailCount = 0;
    private int courseSuccessCount = 0;
    private int courseDuplicateCount = 0;
    private int courseFailCount = 0;
    private int experimentSuccessCount = 0;
    private int experimentDuplicateCount = 0;
    private int experimentFailCount = 0;
    private int classExperimentSuccessCount = 0;
    private int classExperimentDuplicateCount = 0;
    private int classExperimentFailCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    public ClassCourseExperimentExcelListener(ClassCourseExperimentImportService importService) {
        this.importService = importService;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(ClassCourseExperimentExcel excel, AnalysisContext context) {
        try {
            // 跳过空行
            if (excel == null || isAllFieldsEmpty(excel)) {
                return;
            }

            // 数据校验
            validateData(excel, context.readRowHolder().getRowIndex() + 1);

            // 添加到批次列表
            dataList.add(excel);

            // 达到批次大小后，执行批量导入
            if (dataList.size() >= BATCH_SIZE) {
                saveData();
            }
        } catch (Exception e) {
            classExperimentFailCount++;
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
        log.info("Excel数据读取完成 - 班级[成功:{}/重复:{}/失败:{}] 课程[成功:{}/重复:{}/失败:{}] 实验[成功:{}/重复:{}/失败:{}] 课次[成功:{}/重复:{}/失败:{}]",
                classSuccessCount, classDuplicateCount, classFailCount,
                courseSuccessCount, courseDuplicateCount, courseFailCount,
                experimentSuccessCount, experimentDuplicateCount, experimentFailCount,
                classExperimentSuccessCount, classExperimentDuplicateCount, classExperimentFailCount);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        super.invokeHeadMap(headMap, context);
    }

    /**
     * 检查是否所有字段都为空
     */
    private boolean isAllFieldsEmpty(ClassCourseExperimentExcel excel) {
        return (excel.getClassName() == null || excel.getClassName().trim().isEmpty())
                && (excel.getCourseName() == null || excel.getCourseName().trim().isEmpty())
                && (excel.getExperimentName() == null || excel.getExperimentName().trim().isEmpty())
                && (excel.getData() == null || excel.getData().trim().isEmpty())
                && (excel.getTime() == null || excel.getTime().trim().isEmpty())
                && (excel.getLaboratory() == null || excel.getLaboratory().trim().isEmpty())
                && (excel.getLaboratory2() == null || excel.getLaboratory2().trim().isEmpty())
                && (excel.getTeacherName() == null || excel.getTeacherName().trim().isEmpty());
    }

    /**
     * 数据校验
     *
     * @param excel    Excel数据
     * @param rowIndex 行号
     */
    private void validateData(ClassCourseExperimentExcel excel, int rowIndex) {
        if (excel.getClassName() == null || excel.getClassName().trim().isEmpty()) {
            throw new BusinessException(400, "班级名称不能为空");
        }
        if (excel.getCourseName() == null || excel.getCourseName().trim().isEmpty()) {
            throw new BusinessException(400, "课程名称不能为空");
        }
        if (excel.getExperimentName() == null || excel.getExperimentName().trim().isEmpty()) {
            throw new BusinessException(400, "实验名称不能为空");
        }
        if (excel.getData() == null || excel.getData().trim().isEmpty()) {
            throw new BusinessException(400, "日期不能为空");
        }
        if (excel.getTime() == null || excel.getTime().trim().isEmpty()) {
            throw new BusinessException(400, "时间不能为空");
        }

        // 日期格式校验：YYYY-MM-dd
        String dateStr = excel.getData().trim();
        if (!dateStr.matches("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}")) {
            throw new BusinessException(400, "日期格式错误，正确格式为 YYYY-MM-dd，实际值: " + dateStr);
        }

        // 时间格式校验：H:mm-H:mm 或 HH:mm-HH:mm
        String timeStr = excel.getTime().trim();
        if (!timeStr.matches("\\d{1,2}:\\d{2}-\\d{1,2}:\\d{2}")) {
            throw new BusinessException(400, "时间格式错误，正确格式为 H:mm-H:mm，实际值: " + timeStr);
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
            var response = importService.batchImport(dataList);

            // 更新统计信息
            classSuccessCount += response.getClassSuccessCount();
            classDuplicateCount += response.getClassDuplicateCount();
            classFailCount += response.getClassFailCount();
            courseSuccessCount += response.getCourseSuccessCount();
            courseDuplicateCount += response.getCourseDuplicateCount();
            courseFailCount += response.getCourseFailCount();
            experimentSuccessCount += response.getExperimentSuccessCount();
            experimentDuplicateCount += response.getExperimentDuplicateCount();
            experimentFailCount += response.getExperimentFailCount();
            classExperimentSuccessCount += response.getClassExperimentSuccessCount();
            classExperimentDuplicateCount += response.getClassExperimentDuplicateCount();
            classExperimentFailCount += response.getClassExperimentFailCount();
            errorMessages.addAll(response.getErrorMessages());

            // 清空批次列表
            dataList.clear();

        } catch (Exception e) {
            log.error("批量导入失败", e);
            classExperimentFailCount += dataList.size();
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
                classSuccessCount, classDuplicateCount, classFailCount,
                courseSuccessCount, courseDuplicateCount, courseFailCount,
                experimentSuccessCount, experimentDuplicateCount, experimentFailCount,
                classExperimentSuccessCount, classExperimentDuplicateCount, classExperimentFailCount,
                errorMessages
        );
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private final int classSuccessCount;
        private final int classDuplicateCount;
        private final int classFailCount;
        private final int courseSuccessCount;
        private final int courseDuplicateCount;
        private final int courseFailCount;
        private final int experimentSuccessCount;
        private final int experimentDuplicateCount;
        private final int experimentFailCount;
        private final int classExperimentSuccessCount;
        private final int classExperimentDuplicateCount;
        private final int classExperimentFailCount;
        private final List<String> errorMessages;

        public ImportResult(int classSuccessCount, int classDuplicateCount, int classFailCount,
                            int courseSuccessCount, int courseDuplicateCount, int courseFailCount,
                            int experimentSuccessCount, int experimentDuplicateCount, int experimentFailCount,
                            int classExperimentSuccessCount, int classExperimentDuplicateCount, int classExperimentFailCount,
                            List<String> errorMessages) {
            this.classSuccessCount = classSuccessCount;
            this.classDuplicateCount = classDuplicateCount;
            this.classFailCount = classFailCount;
            this.courseSuccessCount = courseSuccessCount;
            this.courseDuplicateCount = courseDuplicateCount;
            this.courseFailCount = courseFailCount;
            this.experimentSuccessCount = experimentSuccessCount;
            this.experimentDuplicateCount = experimentDuplicateCount;
            this.experimentFailCount = experimentFailCount;
            this.classExperimentSuccessCount = classExperimentSuccessCount;
            this.classExperimentDuplicateCount = classExperimentDuplicateCount;
            this.classExperimentFailCount = classExperimentFailCount;
            this.errorMessages = errorMessages;
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

        public int getCourseSuccessCount() {
            return courseSuccessCount;
        }

        public int getCourseDuplicateCount() {
            return courseDuplicateCount;
        }

        public int getCourseFailCount() {
            return courseFailCount;
        }

        public int getExperimentSuccessCount() {
            return experimentSuccessCount;
        }

        public int getExperimentDuplicateCount() {
            return experimentDuplicateCount;
        }

        public int getExperimentFailCount() {
            return experimentFailCount;
        }

        public int getClassExperimentSuccessCount() {
            return classExperimentSuccessCount;
        }

        public int getClassExperimentDuplicateCount() {
            return classExperimentDuplicateCount;
        }

        public int getClassExperimentFailCount() {
            return classExperimentFailCount;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }
}
