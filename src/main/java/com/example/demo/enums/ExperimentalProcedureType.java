package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum ExperimentalProcedureType {
    WatchTheVideo("观看视频"),
    DataCollection("数据收集"),
    AnswerQuestions("题库答题"),
    SubmitTheExperimentReport("提交实验报告"),
    TimedQuiz("限时答题");

    public final String type;

    ExperimentalProcedureType(String type) {
        this.type = type;
    }
}
