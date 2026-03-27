package com.example.demo.util;

import com.example.demo.pojo.entity.Topic;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 题目答案契约工具类
 *
 * <p>统一规则：
 * <ul>
 *     <li>单选题：使用选项字母，例如 A</li>
 *     <li>多选题：使用横杠分隔的选项字母，例如 A-B-C</li>
 *     <li>判断题：接口标准值使用 "正确" / "错误"，内部统一存储为 T / F</li>
 *     <li>填空题、简答题：使用原始文本</li>
 * </ul>
 *
 * <p>为避免影响存量数据，判断题仍兼容旧值 A/B/T/F。
 */
public final class TopicAnswerContractUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String JUDGMENT_TRUE_STORAGE = "T";
    public static final String JUDGMENT_FALSE_STORAGE = "F";
    public static final String JUDGMENT_TRUE_API = "正确";
    public static final String JUDGMENT_FALSE_API = "错误";

    private TopicAnswerContractUtil() {
    }

    /**
     * 将答案规范化为写库格式。
     *
     * @throws IllegalArgumentException 判断题答案非法时抛出
     */
    public static String normalizeForWrite(Integer topicType, String answer) {
        if (answer == null) {
            return null;
        }

        String trimmed = answer.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        if (Integer.valueOf(1).equals(topicType)) {
            return normalizeSingleChoice(trimmed);
        }

        if (Integer.valueOf(2).equals(topicType)) {
            return normalizeMultiChoice(trimmed);
        }

        if (!isJudgment(topicType)) {
            return trimmed;
        }

        String normalized = normalizeJudgmentLenient(trimmed);
        if (normalized == null) {
            throw new IllegalArgumentException("判断题答案必须使用 正确/错误（兼容 A/B/T/F）");
        }
        return normalized;
    }

    /**
     * 将答案规范化为接口返回值。
     */
    public static String normalizeForApi(Integer topicType, String answer) {
        if (answer == null) {
            return null;
        }

        String trimmed = answer.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        if (!isJudgment(topicType)) {
            return trimmed;
        }

        String normalized = normalizeJudgmentLenient(trimmed);
        if (JUDGMENT_TRUE_STORAGE.equals(normalized)) {
            return JUDGMENT_TRUE_API;
        }
        if (JUDGMENT_FALSE_STORAGE.equals(normalized)) {
            return JUDGMENT_FALSE_API;
        }
        return trimmed;
    }

    /**
     * 判断两个答案是否语义相等。
     */
    public static boolean answersEqual(Integer topicType, String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        if (!isJudgment(topicType)) {
            return left.trim().equals(right.trim());
        }

        String normalizedLeft = normalizeJudgmentLenient(left.trim());
        String normalizedRight = normalizeJudgmentLenient(right.trim());
        if (normalizedLeft == null || normalizedRight == null) {
            return left.trim().equals(right.trim());
        }
        return normalizedLeft.equals(normalizedRight);
    }

    /**
     * 按题目类型规范化一组学生答案，供写库使用。
     */
    public static Map<Long, String> normalizeAnswerMapForWrite(
            List<Topic> topics, Map<Long, String> answers) {
        Map<Long, String> normalizedAnswers = new HashMap<>();
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("答案不能为空");
        }

        Map<Long, Topic> topicMap = new HashMap<>();
        if (topics != null) {
            for (Topic topic : topics) {
                topicMap.put(topic.getId(), topic);
            }
        }

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Topic topic = topicMap.get(entry.getKey());
            if (topic == null) {
                throw new IllegalArgumentException("提交了无效的题目ID: " + entry.getKey());
            }
            normalizedAnswers.put(entry.getKey(), normalizeAndValidateForWrite(topic, entry.getValue()));
        }

        return normalizedAnswers;
    }

    public static boolean isJudgment(Integer topicType) {
        return Integer.valueOf(3).equals(topicType);
    }

    private static String normalizeJudgmentLenient(String answer) {
        if (answer == null) {
            return null;
        }

        String upper = answer.trim().toUpperCase();
        return switch (upper) {
            case "T", "A", "正确" -> JUDGMENT_TRUE_STORAGE;
            case "F", "B", "错误" -> JUDGMENT_FALSE_STORAGE;
            default -> null;
        };
    }

    private static String normalizeAndValidateForWrite(Topic topic, String answer) {
        String normalized = normalizeForWrite(topic.getType(), answer);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException(buildTopicPrefix(topic) + "答案不能为空");
        }

        if (Integer.valueOf(1).equals(topic.getType())) {
            validateChoiceAnswer(topic, List.of(normalized));
            return normalized;
        }

        if (Integer.valueOf(2).equals(topic.getType())) {
            List<String> optionKeys = splitOptionKeys(normalized);
            validateChoiceAnswer(topic, optionKeys);
            return String.join("-", optionKeys);
        }

        return normalized;
    }

    private static String normalizeSingleChoice(String answer) {
        String upper = answer.trim().toUpperCase();
        if (!upper.matches("^[A-Z]$")) {
            throw new IllegalArgumentException("单选题答案格式错误，应为单个选项字母，如 A");
        }
        return upper;
    }

    private static String normalizeMultiChoice(String answer) {
        List<String> optionKeys = splitOptionKeys(answer);
        if (optionKeys.isEmpty()) {
            throw new IllegalArgumentException("多选题答案格式错误，应为 A-B-C");
        }
        Collections.sort(optionKeys);
        return String.join("-", optionKeys);
    }

    private static List<String> splitOptionKeys(String answer) {
        String[] parts = answer.split("-");
        List<String> optionKeys = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String part : parts) {
            String token = part == null ? "" : part.trim().toUpperCase();
            if (!token.matches("^[A-Z]$")) {
                throw new IllegalArgumentException("多选题答案格式错误，应为 A-B-C");
            }
            if (!seen.add(token)) {
                throw new IllegalArgumentException("多选题答案格式错误，不能包含重复选项");
            }
            optionKeys.add(token);
        }
        return optionKeys;
    }

    private static void validateChoiceAnswer(Topic topic, List<String> optionKeys) {
        Map<String, String> choiceMap = parseChoiceMap(topic);
        for (String optionKey : optionKeys) {
            if (!choiceMap.containsKey(optionKey)) {
                throw new IllegalArgumentException(buildTopicPrefix(topic) + "答案包含无效选项: " + optionKey);
            }
        }
    }

    private static Map<String, String> parseChoiceMap(Topic topic) {
        String choices = topic.getChoices();
        if (choices == null || choices.isBlank()) {
            throw new IllegalArgumentException(buildTopicPrefix(topic) + "题目缺少选项配置");
        }

        try {
            return MAPPER.readValue(choices, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException(buildTopicPrefix(topic) + "题目选项配置无效");
        }
    }

    private static String buildTopicPrefix(Topic topic) {
        if (topic == null || topic.getNumber() == null) {
            return "";
        }
        return "第" + topic.getNumber() + "题";
    }
}
