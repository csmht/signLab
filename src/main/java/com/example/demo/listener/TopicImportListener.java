package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.entity.Tag;
import com.example.demo.pojo.excel.TopicImportExcel;
import com.example.demo.service.TagService;
import com.example.demo.service.TopicService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 题目导入监听器
 * 用于读取Excel数据并进行批量导入
 */
@Slf4j
public class TopicImportListener extends AnalysisEventListener<TopicImportExcel> {

    /**
     * 批量插入的批次大小
     */
    private static final int BATCH_SIZE = 50;

    /**
     * 临时存储读取到的题目数据
     */
    private final List<TopicImportExcel> topicList = new ArrayList<>();

    /**
     * 题目服务
     */
    private final TopicService topicService;

    /**
     * 标签服务
     */
    private final TagService tagService;

    /**
     * 导入结果统计
     */
    private int successCount = 0;
    private int failCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    /**
     * 标签缓存，避免重复查询
     */
    private final Map<String, Long> tagCache = new HashMap<>();

    /**
     * 当前操作用户
     */
    private final String username;

    public TopicImportListener(TopicService topicService, TagService tagService, String username) {
        this.topicService = topicService;
        this.tagService = tagService;
        this.username = username;
    }

    @Override
    public void invoke(TopicImportExcel data, AnalysisContext context) {
        try {
            // 跳过空行
            if (data == null || (data.getContent() == null && data.getTopicType() == null)) {
                return;
            }

            // 数据校验
            validateData(data);

            // 添加到列表
            topicList.add(data);

            // 达到批次大小后，执行批量插入
            if (topicList.size() >= BATCH_SIZE) {
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
        if (!topicList.isEmpty()) {
            saveData();
        }
        log.info("Excel数据读取完成，成功：{}条，失败：{}条", successCount, failCount);
    }

    /**
     * 数据校验
     */
    private void validateData(TopicImportExcel data) {
        // 校验题目内容
        if (data.getContent() == null || data.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "题目内容不能为空");
        }

        // 校验题目类型
        if (data.getTopicType() == null || data.getTopicType().trim().isEmpty()) {
            throw new BusinessException(400, "题目类型不能为空");
        }

        // 校验答案
        if (data.getCorrectAnswer() == null || data.getCorrectAnswer().trim().isEmpty()) {
            throw new BusinessException(400, "题目答案不能为空");
        }

        // 校验选项（单选题和多选题必须有选项）
        String topicType = data.getTopicType().trim();
        if ("单选题".equals(topicType) || "多选题".equals(topicType)) {
            if (data.getChoices() == null || data.getChoices().trim().isEmpty()) {
                throw new BusinessException(400, "单选题或多选题必须有选项");
            }
        }

        // 校验判断题的答案
        if ("判断题".equals(topicType)) {
            String answer = data.getCorrectAnswer().trim().toUpperCase();
            if (!"A".equals(answer) && !"B".equals(answer) && !"正确".equals(answer) && !"错误".equals(answer)) {
                throw new BusinessException(400, "判断题答案必须是 A/B 或 正确/错误");
            }
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (topicList.isEmpty()) {
            return;
        }

        try {
            // 批量创建题目
            for (TopicImportExcel excelData : topicList) {
                try {
                    createTopicFromExcel(excelData);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("导入失败: " + e.getMessage());
                    log.error("导入题目失败: {}", excelData.getContent(), e);
                }
            }
            topicList.clear();
        } catch (Exception e) {
            log.error("批量保存题目失败", e);
            failCount += topicList.size();
            errorMessages.add("批量保存失败: " + e.getMessage());
            topicList.clear();
        }
    }

    /**
     * 从Excel数据创建题目
     */
    private void createTopicFromExcel(TopicImportExcel excelData) {
        // 1. 解析题目类型
        Integer type = parseTopicType(excelData.getTopicType());

        // 2. 获取选项（判断题不需要选项）
        String choices = null;
        if (type != 3) {
            choices = excelData.getChoices();
            // 验证JSON格式是否正确
            if (choices != null && !choices.trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.readValue(choices, java.util.Map.class);
                } catch (Exception e) {
                    throw new BusinessException(400, "选项JSON格式错误: " + e.getMessage());
                }
            }
        }

        // 3. 解析答案
        String correctAnswer = parseCorrectAnswer(excelData, type);

        // 4. 解析标签
        List<Long> tagIds = parseTagIds(excelData);

        // 5. 创建题目请求
        com.example.demo.pojo.request.teacher.CreateTopicRequest request =
            new com.example.demo.pojo.request.teacher.CreateTopicRequest();
        request.setType(type);
        request.setContent(excelData.getContent().trim());
        request.setChoices(choices);
        request.setCorrectAnswer(correctAnswer);
        request.setTagIds(tagIds);

        // 6. 调用服务创建题目
        topicService.createTopic(request, username);
    }

    /**
     * 解析题目类型
     */
    private Integer parseTopicType(String topicType) {
        if (topicType == null) {
            throw new BusinessException(400, "题目类型不能为空");
        }

        String type = topicType.trim();
        switch (type) {
            case "单选题":
                return 1;
            case "多选题":
                return 2;
            case "判断题":
                return 3;
            default:
                throw new BusinessException(400, "不支持的题目类型: " + type + "，仅支持：单选题、多选题、判断题");
        }
    }

    /**
     * 解析答案
     */
    private String parseCorrectAnswer(TopicImportExcel excelData, Integer type) {
        String answer = excelData.getCorrectAnswer().trim();

        if (type == 3) {
            // 判断题：A/B 或 正确/错误
            String upperAnswer = answer.toUpperCase();
            switch (upperAnswer) {
                case "A":
                case "正确":
                    return "A";
                case "B":
                case "错误":
                    return "B";
                default:
                    throw new BusinessException(400, "判断题答案格式错误，请使用 A/B 或 正确/错误");
            }
        }

        // 单选题或多选题：验证答案格式
        if (type == 1) {
            // 单选题：单个字母
            if (!answer.matches("^[A-F]$")) {
                throw new BusinessException(400, "单选题答案格式错误，请使用单个字母（A-F）");
            }
        } else if (type == 2) {
            // 多选题：字母组合，用横杠分隔
            if (!answer.matches("^[A-F](-[A-F])*$")) {
                throw new BusinessException(400, "多选题答案格式错误，请使用字母组合（如：A-B-C）");
            }
        }

        return answer;
    }

    /**
     * 解析标签ID列表
     */
    private List<Long> parseTagIds(TopicImportExcel excelData) {
        List<Long> tagIds = new ArrayList<>();

        // 解析课程标签（学科标签）
        if (excelData.getCourseTag() != null && !excelData.getCourseTag().trim().isEmpty()) {
            Long tagId = getOrCreateTag(excelData.getCourseTag().trim(), "1", "学科标签");
            if (tagId != null) {
                tagIds.add(tagId);
            }
        }

        // 解析难度标签
        if (excelData.getDifficultyTag() != null && !excelData.getDifficultyTag().trim().isEmpty()) {
            Long tagId = getOrCreateTag(excelData.getDifficultyTag().trim(), "2", "难度标签");
            if (tagId != null) {
                tagIds.add(tagId);
            }
        }

        // 解析自定义标签
        if (excelData.getCustomTag() != null && !excelData.getCustomTag().trim().isEmpty()) {
            Long tagId = getOrCreateTag(excelData.getCustomTag().trim(), "4", "自定义标签");
            if (tagId != null) {
                tagIds.add(tagId);
            }
        }

        return tagIds;
    }

    /**
     * 获取或创建标签
     */
    private Long getOrCreateTag(String tagName, String type, String description) {
        // 先从缓存中查找
        String cacheKey = type + ":" + tagName;
        if (tagCache.containsKey(cacheKey)) {
            return tagCache.get(cacheKey);
        }

        // 查询数据库
        Tag existingTag = tagService.lambdaQuery()
                .eq(Tag::getTagName, tagName)
                .eq(Tag::getType, type)
                .one();

        if (existingTag != null) {
            tagCache.put(cacheKey, existingTag.getId());
            return existingTag.getId();
        }

        // 创建新标签
        try {
            Tag newTag = new Tag();
            newTag.setTagName(tagName);
            newTag.setType(type);
            newTag.setDescription(description);
            tagService.save(newTag);
            tagCache.put(cacheKey, newTag.getId());
            log.info("自动创建新标签：{}（类型：{}）", tagName, description);
            return newTag.getId();
        } catch (Exception e) {
            log.error("创建标签失败：{}", tagName, e);
            return null;
        }
    }

    /**
     * 获取导入结果
     */
    public ImportResult getResult() {
        return new ImportResult(successCount, failCount, errorMessages);
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private final int successCount;
        private final int failCount;
        private final List<String> errorMessages;

        public ImportResult(int successCount, int failCount, List<String> errorMessages) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errorMessages = errorMessages;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }
}
