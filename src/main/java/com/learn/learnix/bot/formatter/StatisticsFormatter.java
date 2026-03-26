package com.learn.learnix.bot.formatter;


import com.learn.learnix.domain.dto.SessionStatsDto;

/**
 * Stateless utility that turns a SessionStatsDto into a Telegram-ready HTML string.
 * No Spring bean — instantiate directly or use as a static helper.
 *
 * Example output:
 *
 *   📊 Results — Java Core
 *   ──────────────────────
 *   Score:    7 / 10  (70%)
 *   Duration: 3m 42s
 *   ──────────────────────
 *   Q1  ✅  What is JVM?             4.2s
 *   Q2  ❌  Which are primitives?    9.1s
 *   ...
 *   ──────────────────────
 *   🏆 Great job!
 */
public class StatisticsFormatter {

    private static final String DIVIDER = "───────────────────";

    public static String format(SessionStatsDto stats, boolean isQuizFullyCompleted) {
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, stats, isQuizFullyCompleted);
        appendDivider(sb);
        appendSummary(sb, stats, isQuizFullyCompleted);
        appendDivider(sb);
        appendQuestionBreakdown(sb, stats, isQuizFullyCompleted);
        appendDivider(sb);
        appendRating(sb, stats.getAccuracyPercent());

        return sb.toString();
    }

    // ── Sections ──────────────────────────────────────────────────────────

    private static void appendHeader(StringBuilder sb, SessionStatsDto stats, boolean isQuizFullyCompleted) {
        sb.append("<b>Results — ")
                .append(escapeHtml(stats.getTopicName()))
                .append("</b>\n");
        if (!isQuizFullyCompleted) {
            sb.append("<i>(Particularly completed)</i>\n");
        }
    }

    private static void appendSummary(StringBuilder sb, SessionStatsDto stats, boolean isQuizFullyCompleted) {
        if (!isQuizFullyCompleted) {
            int completed = countOnlyCompleted(stats);
            sb.append(String.format("Only Completed score:       <b>%d / %d</b>  (%.0f%%)\n",
                    stats.getCorrectAnswers(),
                    completed,
                    (stats.getCorrectAnswers() * 100.0) / completed));
        }
        sb.append(String.format("Overall score:       <b>%d / %d</b>  (%.0f%%)\n",
                stats.getCorrectAnswers(),
                stats.getTotalQuestions(),
                stats.getAccuracyPercent()));

        sb.append(String.format("Duration:  <b>%s</b>\n",
                formatDuration(stats.getDurationSeconds())));
    }

    private static int countOnlyCompleted(SessionStatsDto stats) {
        int count = 0;
        for (SessionStatsDto.QuestionStatDto q : stats.getQuestionStats()) {
            if (q.getTimeTakenMs() > 0) {
                count++;
            }
        }
        return count;
    }

    private static void appendQuestionBreakdown(StringBuilder sb, SessionStatsDto stats, boolean isQuizFullyCompleted) {
        for (SessionStatsDto.QuestionStatDto q : stats.getQuestionStats()) {
            String marker = q.isCorrect() ? "✅" : "❌";
            String body   = truncate(q.getQuestionBody(), 35);
            String time   = formatMs(q.getTimeTakenMs());

            sb.append(String.format(
                    "<code>Q%-2d</code>   %s   %-35s  <i>%s</i>\n",
                    q.getSequenceNumber(),
                    !time.equals("—") ? marker : "<i>SKIP</i>",
                    escapeHtml(body),
                    time));
        }
    }

    private static void appendRating(StringBuilder sb, double accuracy) {
        String rating;
        if      (accuracy == 100) rating = "Perfect score!";
        else if (accuracy >= 80)  rating = "Great job!";
        else if (accuracy >= 60)  rating = "Good effort!";
        else if (accuracy >= 40)  rating = "Keep practising!";
        else                      rating = "Don't give up — try again!";

        sb.append("<b>").append(rating).append("</b>\n");
        sb.append("Restart quiz: /restart");
    }

    private static void appendDivider(StringBuilder sb) {
        sb.append(DIVIDER).append("\n");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static String formatDuration(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    private static String formatMs(int ms) {
        if (ms == 0) return "—";
        return String.format("%.1fs", ms / 1000.0);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen
                ? text.substring(0, maxLen - 1) + "…"
                : text;
    }

    /** Minimal HTML escaping — Telegram HTML parse mode needs < > & escaped. */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}