package com.example.demo.util;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.example.demo.pojo.dto.mapvo.TopicAnswerItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答案 JSON 解析工具类
 * 统一处理 student_experimental_procedure 表中 answer 字段的解析
 * 答案格式：{"type": "类型描述", "data": {...}}
 */
public class AnswerMapJSONUntil {

    private static final Logger log = LoggerFactory.getLogger(AnswerMapJSONUntil.class);

    public static String VIEWED = "VIEWED";

    // 类型常量
    public static String TYPE_TOPIC = "TOPIC";                       // 题库答题
    public static String TYPE_TIMED_QUIZ = "TIMED_QUIZ";             // 限时答题
    public static String TYPE_DATA_COLLECTION = "DATA_COLLECTION";   // 数据收集

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 解析答案 JSON，返回 data 字段的内容
     * 根据 type 字段判断是否有嵌套结构：
     * - TOPIC/TIMED_QUIZ: data 中值是字符串，直接返回 Map<String, String>
     * - DATA_COLLECTION: data 中值是嵌套对象，需要特殊处理
     *
     * @param answerJson JSON 字符串
     * @return Map<字段名/题目ID, 答案>，解析失败返回空 Map
     */
    public static Map<String, String> parseData(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<String, Object> answerMap = MAPPER.readValue(answerJson,
                new TypeReference<Map<String, Object>>() {});

            String type = (String) answerMap.get("type");
            Object dataObj = answerMap.get("data");
            if (dataObj == null) {
                return new HashMap<>();
            }

            // 如果 data 是字符串，需要再次解析为 Map
            if (dataObj instanceof String) {
                String dataStr = (String) dataObj;
                if (dataStr.isEmpty()) {
                    return new HashMap<>();
                }

                // 根据 type 判断解析方式
                if (TYPE_DATA_COLLECTION.equals(type)) {
                    // DATA_COLLECTION 类型，data 字符串中包含嵌套对象
                    Map<String, Object> objectMap = MAPPER.readValue(dataStr,
                        new TypeReference<Map<String, Object>>() {});
                    // 将嵌套对象转换为字符串形式返回
                    Map<String, String> result = new HashMap<>();
                    for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                        if (entry.getValue() instanceof Map) {
                            result.put(entry.getKey(), MAPPER.writeValueAsString(entry.getValue()));
                        } else {
                            result.put(entry.getKey(), String.valueOf(entry.getValue()));
                        }
                    }
                    return result;
                } else {
                    // TOPIC/TIMED_QUIZ/VIEWED 类型，直接解析为 String Map
                    return MAPPER.readValue(dataStr, new TypeReference<Map<String, String>>() {});
                }
            }

            // 如果 data 已经是 Map
            if (TYPE_DATA_COLLECTION.equals(type)) {
                // DATA_COLLECTION 类型，data 中包含嵌套对象
                @SuppressWarnings("unchecked")
                Map<String, Object> objectMap = (Map<String, Object>) dataObj;
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        result.put(entry.getKey(), MAPPER.writeValueAsString(entry.getValue()));
                    } else {
                        result.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
                return result;
            } else {
                // TOPIC/TIMED_QUIZ/VIEWED 类型，直接返回 String Map
                @SuppressWarnings("unchecked")
                Map<String, String> dataMap = (Map<String, String>) dataObj;
                return dataMap;
            }
        } catch (Exception e) {
            log.error("解析答案 JSON 失败, answerJson: {}", answerJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 解析答案 JSON，返回 data 字段的内容（支持嵌套对象）
     * 根据 type 字段判断解析方式：
     * - DATA_COLLECTION: 返回包含嵌套对象的 Map<String, Object>
     * - 其他类型: 返回 Map<String, Object>，但值都是字符串
     *
     * @param answerJson JSON 字符串
     * @return Map<字段名, Object>，解析失败返回空 Map
     */
    public static Map<String, Object> parseDataAsObject(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<String, Object> answerMap = MAPPER.readValue(answerJson,
                new TypeReference<Map<String, Object>>() {});

            String type = (String) answerMap.get("type");
            Object dataObj = answerMap.get("data");
            if (dataObj == null) {
                return new HashMap<>();
            }

            // 如果 data 是字符串，需要再次解析为 Map
            if (dataObj instanceof String) {
                String dataStr = (String) dataObj;
                if (dataStr.isEmpty()) {
                    return new HashMap<>();
                }

                // 根据 type 判断解析方式
                if (TYPE_DATA_COLLECTION.equals(type)) {
                    // DATA_COLLECTION 类型，解析为嵌套对象 Map
                    return MAPPER.readValue(dataStr, new TypeReference<Map<String, Object>>() {});
                } else {
                    // TOPIC/TIMED_QUIZ/VIEWED 类型，先解析为 String Map，再转换为 Object Map
                    Map<String, String> stringMap = MAPPER.readValue(dataStr,
                        new TypeReference<Map<String, String>>() {});
                    return new HashMap<>(stringMap);
                }
            }

            // 如果 data 已经是 Map
            if (TYPE_DATA_COLLECTION.equals(type)) {
                // DATA_COLLECTION 类型，直接返回 Object Map
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                return dataMap;
            } else {
                // TOPIC/TIMED_QUIZ/VIEWED 类型，转换为 Object Map
                @SuppressWarnings("unchecked")
                Map<String, ?> rawMap = (Map<String, ?>) dataObj;
                Map<String, Object> result = new HashMap<>();
                for (Map.Entry<String, ?> entry : rawMap.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                return result;
            }
        } catch (Exception e) {
            log.error("解析答案 JSON 失败, answerJson: {}", answerJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 获取答案 JSON 中的 type 字段
     *
     * @param answerJson JSON 字符串
     * @return type 字段值，解析失败返回 null
     */
    public static String parseType(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return null;
        }

        try {
            Map<String, Object> answerMap = MAPPER.readValue(answerJson,
                new TypeReference<Map<String, Object>>() {});

            return (String) answerMap.get("type");
        } catch (Exception e) {
            log.error("解析答案 JSON type 字段失败, answerJson: {}", answerJson, e);
            return null;
        }
    }

    /**
     * 解析题库/限时答题的答案数据，将 String key 转换为 Long key
     *
     * @param answerJson JSON 字符串
     * @return Map<题目ID, 答案>，解析失败返回空 Map
     */
    public static Map<Long, String> parseTopicData(String answerJson) {
        Map<String, String> dataMap = parseData(answerJson);
        Map<Long, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            try {
                Long topicId = Long.parseLong(entry.getKey());
                result.put(topicId, entry.getValue());
            } catch (NumberFormatException e) {
                log.warn("无效的题目ID: {}", entry.getKey());
            }
        }

        return result;
    }

    // ==================== 反向转换方法 ====================

    /**
     * 将类型和数据转换为答案 JSON 字符串
     *
     * @param type 答案类型
     * @param data 数据 Map
     * @return JSON 字符串，转换失败返回 null
     */
    public static String toJson(String type, Map<String, String> data) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("type", type);
        answerMap.put("data", data != null ? data : new HashMap<>());

        try {
            return MAPPER.writeValueAsString(answerMap);
        } catch (Exception e) {
            log.error("转换答案 JSON 失败, type: {}, data: {}", type, data, e);
            return null;
        }
    }

    /**
     * 将类型和数据转换为答案 JSON 字符串（data 为 JSON 字符串格式）
     *
     * @param type 答案类型
     * @param data 数据 Map
     * @return JSON 字符串，data 字段为 JSON 字符串格式，转换失败返回 null
     */
    public static String toJsonWithDataString(String type, Map<String, String> data) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        try {
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("type", type);

            // 将 data 转换为 JSON 字符串
            String dataJson = MAPPER.writeValueAsString(data != null ? data : new HashMap<>());
            answerMap.put("data", dataJson);

            return MAPPER.writeValueAsString(answerMap);
        } catch (Exception e) {
            log.error("转换答案 JSON 失败, type: {}, data: {}", type, data, e);
            return null;
        }
    }

    /**
     * 将题目答案 Map 转换为答案 JSON 字符串（用于题库/限时答题）
     *
     * @param topicAnswers Map<题目ID, 答案>
     * @return JSON 字符串，转换失败返回 null
     */
    public static String toTopicJson(Map<Long, String> topicAnswers) {
        if (topicAnswers == null || topicAnswers.isEmpty()) {
            return null;
        }

        Map<String, String> dataMap = new HashMap<>();
        for (Map.Entry<Long, String> entry : topicAnswers.entrySet()) {
            dataMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return toJson(TYPE_TOPIC, dataMap);
    }

    /**
     * 将题目答案 Map 转换为答案 JSON 字符串（用于限时答题）
     *
     * @param topicAnswers Map<题目ID, 答案>
     * @return JSON 字符串，转换失败返回 null
     */
    public static String toTimedQuizJson(Map<Long, String> topicAnswers) {
        if (topicAnswers == null || topicAnswers.isEmpty()) {
            return null;
        }

        Map<String, String> dataMap = new HashMap<>();
        for (Map.Entry<Long, String> entry : topicAnswers.entrySet()) {
            dataMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return toJson(TYPE_TIMED_QUIZ, dataMap);
    }

    /**
     * 将数据收集答案转换为答案 JSON 字符串
     *
     * @param fillBlankAnswers 填空答案 Map<字段名, 答案>
     * @param tableCellAnswers 表格单元格答案 Map<单元格位置, 答案>
     * @return JSON 字符串，转换失败返回 null
     */
    public static String toDataCollectionJson(Map<String, String> fillBlankAnswers,
                                               Map<String, String> tableCellAnswers) {
        Map<String, Object> dataMap = new HashMap<>();
        if (fillBlankAnswers != null && !fillBlankAnswers.isEmpty()) {
            dataMap.put("fillBlankAnswers", fillBlankAnswers);
        }
        if (tableCellAnswers != null && !tableCellAnswers.isEmpty()) {
            dataMap.put("tableCellAnswers", tableCellAnswers);
        }

        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("type", TYPE_DATA_COLLECTION);
        answerMap.put("data", dataMap);

        try {
            return MAPPER.writeValueAsString(answerMap);
        } catch (Exception e) {
            log.error("转换数据收集答案 JSON 失败", e);
            return null;
        }
    }

    /**
     * 构建文件上传类型的答案 JSON 字符串（数据收集 type=3）
     * 文件数据类型只需要上传文件，答案 JSON 中只包含 dataType 标识
     *
     * @return JSON 字符串
     */
    public static String toFileUploadJson() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("dataType", 3);

        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("type", TYPE_DATA_COLLECTION);
        answerMap.put("data", dataMap);

        try {
            return MAPPER.writeValueAsString(answerMap);
        } catch (Exception e) {
            log.error("转换文件上传答案 JSON 失败", e);
            return null;
        }
    }

    // ==================== DTO 列表便捷构建方法 ====================

    /**
     * 从 TopicAnswerItem 列表构建题库答题答案 JSON
     *
     * @param answers 题目答案项列表
     * @return JSON 字符串，转换失败返回 null
     */
    public static String buildTopicAnswerJson(List<TopicAnswerItem> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        Map<Long, String> answerMap = TopicAnswerItem.toMap(answers);
        return toTopicJson(answerMap);
    }

    /**
     * 从 TopicAnswerItem 列表构建限时答题答案 JSON
     *
     * @param answers 题目答案项列表
     * @return JSON 字符串，转换失败返回 null
     */
    public static String buildTimedQuizAnswerJson(List<TopicAnswerItem> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        Map<Long, String> answerMap = TopicAnswerItem.toMap(answers);
        return toTimedQuizJson(answerMap);
    }

    /**
     * 从 FillBlankAnswer 列表构建填空答案 JSON（数据收集）
     *
     * @param fillBlankAnswers 填空答案列表
     * @return JSON 字符串，转换失败返回 null
     */
    public static String buildDataCollectionAnswerJson(List<FillBlankAnswer> fillBlankAnswers) {
        return buildDataCollectionAnswerJson(fillBlankAnswers, null);
    }

    /**
     * 从 FillBlankAnswer 列表和 TableCellAnswer 列表构建数据收集答案 JSON
     *
     * @param fillBlankAnswers 填空答案列表
     * @param tableCellAnswers 表格单元格答案列表
     * @return JSON 字符串，转换失败返回 null
     */
    public static String buildDataCollectionAnswerJson(List<FillBlankAnswer> fillBlankAnswers,
                                                        List<TableCellAnswer> tableCellAnswers) {
        Map<String, String> fillBlankMap = FillBlankAnswer.toMap(fillBlankAnswers);
        Map<String, String> tableCellMap = TableCellAnswer.toMap(tableCellAnswers);
        return toDataCollectionJson(fillBlankMap, tableCellMap);
    }

    /**
     * 从单个题目 ID 和答案构建题库答题答案 JSON
     *
     * @param topicId 题目 ID
     * @param answer  答案
     * @return JSON 字符串，转换失败返回 null
     */
    public static String buildSingleTopicAnswerJson(Long topicId, String answer) {
        if (topicId == null || answer == null) {
            return null;
        }
        Map<Long, String> answerMap = new HashMap<>();
        answerMap.put(topicId, answer);
        return toTopicJson(answerMap);
    }

    public static String buildVideo() {
        return toJson(VIEWED, new HashMap<>());
    }

}



