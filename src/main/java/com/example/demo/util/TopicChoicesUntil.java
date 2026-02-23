package com.example.demo.util;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.json.JSONFilter;
import org.springframework.boot.json.JacksonJsonParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicChoicesUntil {

    /**
     * 解析题库答案JSON
     */
    public static Map<Long, String> parseTopicAnswers(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<Long, String> longStringMap = new HashMap<>();

            String[] split = answerJson.split(";");
            for (String s : split) {
                String[] keyValue = s.split(":");
                longStringMap.put(Long.parseLong(keyValue[0]), keyValue[1]);
            }

            return longStringMap;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 解析题目选项字符串为Map
     * 格式：A:选项A内容$B:选项B内容$C:选项C内容$D:选项D内容
     * @param choices 选项字符串
     * @return 选项Map，key为选项字母(A、B、C、D)，value为选项内容
     */
    public static Map<String, String> parseTopicChoices(String choices) {
        if (choices == null || choices.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> choicesMap = new HashMap<>();
        try {
            // 按 $ 分割各个选项
            String[] options = choices.split("\\$");
            for (String option : options) {
                // 每个选项格式为 "A:选项内容"
                if (option.contains(":")) {
                    String[] parts = option.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        choicesMap.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            return new HashMap<>();
        }

        return choicesMap;
    }

    public static String MapJson(Map<String, String> map) throws JsonProcessingException {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

    public static Map<String,String> JsonToMap(String json){
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            // 去掉外层的双引号（如果有）
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }
            // 替换转义的双引号 \" 为 普通双引号 "
            json = json.replace("\\\"", "\"");

            Map<String, String> map = new HashMap<>();

            ObjectMapper mapper = new ObjectMapper();
            JacksonJsonParser jacksonJsonParser = new JacksonJsonParser(mapper);
            Map<String, Object> stringObjectMap = jacksonJsonParser.parseMap(json);

            stringObjectMap.forEach((key, value) -> {
                map.put(key, value.toString());
            });
            return map;
        }catch (Exception e){
            throw new BusinessException("参数格式错误");
        }
    }

    /**
     * 将 Map<Long, String> 转为 JSON 字符串
     * @param map Map 对象
     * @return JSON 字符串
     */
    public static String LongStringMapToJson(Map<Long, String> map) throws JsonProcessingException {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

    /**
     * 将 JSON 字符串转为 Map<Long, String>
     * @param json JSON 字符串
     * @return Map<Long, String> 对象
     */
    public static Map<Long, String> JsonToLongStringMap(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            // 去掉外层的双引号（如果有）
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }
            // 替换转义的双引号
            json = json.replace("\\\"", "\"");

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<Map<Long, String>>() {});
        } catch (Exception e) {
            throw new BusinessException("参数格式错误");
        }
    }

    /**
     * 将 List<Map<String, Object>> 转为 JSON 字符串
     * @param list List 对象
     * @return JSON 字符串
     */
    public static String listMapToJson(List<Map<String, Object>> list) throws JsonProcessingException {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(list);
    }

    /**
     * 将 Map<String, Object> 转为 JSON 字符串
     * @param map Map 对象
     * @return JSON 字符串
     */
    public static String objectMapToJson(Map<String, Object> map) throws JsonProcessingException {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }
}
