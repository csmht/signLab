package com.example.demo.util;

import com.example.demo.pojo.entity.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return normalizedAnswers;
        }

        Map<Long, Integer> topicTypeMap = new HashMap<>();
        if (topics != null) {
            for (Topic topic : topics) {
                topicTypeMap.put(topic.getId(), topic.getType());
            }
        }

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Integer topicType = topicTypeMap.get(entry.getKey());
            normalizedAnswers.put(entry.getKey(), normalizeForWrite(topicType, entry.getValue()));
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
}
