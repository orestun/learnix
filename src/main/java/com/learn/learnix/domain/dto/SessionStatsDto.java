package com.learn.learnix.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SessionStatsDto {

    // ── Session-level ─────────────────────────────────────────────────────

    private String  topicName;
    private int     totalQuestions;
    private int     correctAnswers;
    private long    durationSeconds;    // total time from startedAt to finishedAt

    // ── Derived ───────────────────────────────────────────────────────────

    public double getAccuracyPercent() {
        if (totalQuestions == 0) return 0.0;
        return (correctAnswers * 100.0) / totalQuestions;
    }

    // ── Per-question breakdown ────────────────────────────────────────────

    private List<QuestionStatDto> questionStats;

    @Getter
    @Builder
    public static class QuestionStatDto {
        private int     sequenceNumber;    // 1-based display index
        private String  questionBody;
        private boolean correct;
        private int     timeTakenMs;
    }
}
