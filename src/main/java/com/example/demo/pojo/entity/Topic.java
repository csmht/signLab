package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.exception.BusinessException;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AutoTable
@Table(value = "topics", comment = "题目表 - 存储试卷中的题目信息")
@TableName("topics")
@TableIndex(name = "idx_paper_id", fields = {"paperId"})
public class Topic {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "试卷ID", type = "bigint")
    private Long paperId;

    @Column(comment = "题号", type = "int")
    private Integer number;

    @Column(comment = "题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他", type = "int")
    private Integer type;

    @Column(comment = "题目内容", type = "varchar(255)")
    private String content;

    @Column(comment = "选项内容", type = "varchar(255)")
    private String choices;

    @Column(comment = "正确答案", type = "varchar(255)")
    private String correctAnswer;

    @Column(comment = "是否删除：0-未删除，1-已删除", type = "bit", defaultValue = "0")
    private Boolean isDeleted;

    public static String choiceToString(Map<String, String> choices) {
        StringBuilder sb = new StringBuilder();
        if(choices == null || choices.isEmpty()){
            return "";
        }
        int size = choices.size();
        int i = 0;
        for (Map.Entry<String, String> entry : choices.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            if (i < size - 1) {
                sb.append("$");
            }
            i++;
        }
        return sb.toString();
    }

    public static Map<String, String> stringToChoice(String choices)throws BusinessException {
        Map<String, String> map = new HashMap<>();
        String[] choiceArray = choices.split("\\$");
        for (String choice : choiceArray) {
            String[] keyValue = choice.split(":");
            if(keyValue.length != 2){
                throw new BusinessException("每个选项的格式必须为 '选项字母:选项内容'");
            }
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    public static Map<String, String> parseFillBlankAnswer(String answer) throws BusinessException {
        Map<String, String> map = new HashMap<>();
        if (answer == null || answer.trim().isEmpty()) {
            return map;
        }

        if (answer.contains("$")) {
            String[] answerArray = answer.split("\\$");
            for (String answerPair : answerArray) {
                String[] keyValue = answerPair.split(":");
                if (keyValue.length != 2) {
                    throw new BusinessException("填空题答案格式必须为 '空编号:答案'");
                }
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        } else {
            map.put("1", answer.trim());
        }
        return map;
    }

    public static String formatFillBlankAnswer(Map<String, String> answerMap) {
        if (answerMap == null || answerMap.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int size = answerMap.size();
        int i = 0;

        List<String> sortedKeys = new java.util.ArrayList<>(answerMap.keySet());
        java.util.Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            sb.append(key).append(":").append(answerMap.get(key));
            if (i < size - 1) {
                sb.append("$");
            }
            i++;
        }
        return sb.toString();
    }

}
