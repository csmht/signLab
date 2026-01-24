package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum ExperimentalProcedureType {
    WatchTheVideo("观看视频"),
    DataCollection("数据收集"),
    AnswerQuestions("题库答题"),
    submitTheExperimentReport("提交实验报告");

    public final String type;

    ExperimentalProcedureType(String type) {
        this.type = type;
    }
}
