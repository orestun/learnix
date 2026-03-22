package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.keyboard.ChoicesKeyboardFactory;
import com.learn.learnix.domain.Choice;
import com.learn.learnix.domain.SessionQuestion;
import com.learn.learnix.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class QuestionSender {

    private final UserStateManager stateManager;

    public void send(AbsSender bot, Long chatId,
                     Long telegramId, SessionQuestion sq) throws Exception {

        String text = buildQuestionText(sq);

        Message sent = bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(ChoicesKeyboardFactory.build(
                        sq.getQuestion().getChoices(),
                        Collections.emptySet()))   // nothing selected yet
                .build());

        // Store message ID and timestamp — both needed by QuestionHandler
        var state = stateManager.getState(telegramId);
        state.setActiveMessageId(sent.getMessageId());
        state.setQuestionSentAt(System.currentTimeMillis());
    }

    private String buildQuestionText(SessionQuestion sq) {
        int total    = sq.getSession().getTotalQuestions();
        int current  = sq.getSequenceOrder() + 1;

        StringBuilder questionText = new StringBuilder();

        String title = String.format(
                "<b>Question %d / %d</b>\n\n",
                current, total);
        String question = String.format(
                "<b><i>%s</i></b>",
                escapeHtml(sq.getQuestion().getBody())
        );

        questionText.append(title).append(question);

        for (Choice choice : sq.getQuestion().getChoices()) {
            questionText.append("\n\n")
                    .append((char)(96 + choice.getDisplayOrder()))
                    .append(") ")
                    .append(choice.getBody());
        }

        return questionText.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
