package com.learn.learnix.bot.formatter;

import com.learn.learnix.domain.Choice;
import com.learn.learnix.domain.SessionQuestion;

import java.util.Set;

/**
 * Stateless utility that formats the inline result message shown immediately
 * after a user submits their answer for a question.
 *
 * Replaces the question+keyboard message in-place via EditMessageText,
 * so the user sees the result without any new message cluttering the chat.
 *
 * Four-state marker system for each choice:
 *   ✅  correct answer, user selected it        — got it right
 *   ☑️  correct answer, user did NOT select it  — missed it
 *   ❌  wrong answer,   user selected it        — incorrect pick
 *   ◻️  wrong answer,   user did NOT select it  — correctly skipped
 *
 * Example output (wrong answer):
 *
 *   ❌ Wrong!
 *
 *   Which are valid Java access modifiers?
 *
 *   ✅ public
 *   ☑️ protected   ← correct but you missed it
 *   ❌ internal    ← wrong but you picked it
 *   ◻️ private
 *   ✅ default
 */
public class ResultFormatter {

    public static String format(SessionQuestion sq, boolean correct, Set<Long> selectedIds) {
        StringBuilder sb = new StringBuilder();

        appendVerdict(sb, correct);
        appendTitle(sb, sq);
        appendQuestionBody(sb, sq);
        appendChoices(sb, sq, selectedIds);

        return sb.toString();
    }

    // ── Sections ──────────────────────────────────────────────────────────

    private static void appendVerdict(StringBuilder sb, boolean correct) {
        sb.append(correct ? "✅ <b>Correct!</b>" : "❌ <b>Wrong!</b>").append("\n");
    }

    private static void appendQuestionBody(StringBuilder sb, SessionQuestion sq) {
        sb.append("<b>")
                .append(escapeHtml(sq.getQuestion().getBody()))
                .append("</b>\n\n");
    }

    private static void appendChoices(StringBuilder sb, SessionQuestion sq,
                                      Set<Long> selectedIds) {
        for (Choice choice : sq.getQuestion().getChoices()) {
            boolean wasSelected = selectedIds.contains(choice.getId());
            boolean isCorrect   = choice.getIsCorrect();

            sb.append(markerFor(isCorrect, wasSelected))
                    .append(" ")
                    .append(escapeHtml(choice.getBody()))
                    .append("\n");
        }
    }

    private static void appendTitle(StringBuilder sb, SessionQuestion sq) {
        int total    = sq.getSession().getTotalQuestions();
        int current  = sq.getSequenceOrder() + 1;

        sb.append(String.format(
                "<b>Question %d / %d</b>\n\n",
                current, total));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Maps the (isCorrect, wasSelected) pair to a visual marker.
     *
     *  isCorrect | wasSelected | marker | meaning
     *  --------- | ----------- | ------ | -------
     *  true      | true        | ✅     | right answer, picked correctly
     *  true      | false       | ☑️     | right answer, user missed it
     *  false     | true        | ❌     | wrong answer, user picked it
     *  false     | false       | ◻️     | wrong answer, correctly skipped
     */
    private static String markerFor(boolean isCorrect, boolean wasSelected) {
        if (isCorrect && wasSelected)  return "✅";
        if (isCorrect)                 return "☑️";
        if (wasSelected)               return "❌";
        return "◻️";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}