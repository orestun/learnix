package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.formatter.ResultFormatter;
import com.learn.learnix.bot.formatter.StatisticsFormatter;
import com.learn.learnix.bot.keyboard.ChoicesKeyboardFactory;
import com.learn.learnix.domain.SessionQuestion;
import com.learn.learnix.domain.dto.SessionStatsDto;
import com.learn.learnix.service.*;
import com.learn.learnix.state.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class QuestionHandler {

    private final SessionService          sessionService;
    private final AnswerEvaluationService evaluationService;
    private final StatisticsService       statisticsService;
    private final UserStateManager        stateManager;
    private final QuestionSender          questionSender;

    public void handle(AbsSender bot, CallbackQuery callback) throws Exception {
        Long   telegramId = callback.getFrom().getId();
        Long   chatId     = callback.getMessage().getChatId();
        String data       = callback.getData();

        UserQuizState state = stateManager.getState(telegramId);
        boolean isCallBackExit = "exit".equals(data);

        if (data.startsWith("c:")) {
            handleToggle(bot, callback, state, chatId, telegramId);

        } else if ("submit".equals(data) || isCallBackExit) {
            handleSubmitOrExit(bot, callback, state, telegramId, chatId, isCallBackExit);
        }
    }

    private void handleToggle(AbsSender bot, CallbackQuery callback,
                              UserQuizState state, Long chatId,
                              Long telegramId) throws Exception {
        Long choiceId = Long.parseLong(callback.getData().replace("c:", ""));
        state.toggleChoice(choiceId);

        SessionQuestion sq = sessionService.getCurrentQuestion(telegramId);

        bot.execute(EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(state.getActiveMessageId())
                .replyMarkup(ChoicesKeyboardFactory.build(
                        sq.getQuestion().getChoices(),
                        state.getSelectedChoiceIds()))
                .build());
    }

    private void handleSubmitOrExit(AbsSender bot, CallbackQuery callback,
                                    UserQuizState state, Long telegramId,
                                    Long chatId, Boolean isCallBackExit) throws Exception {

        // Guard: require at least one selection
        if (!state.hasSelections() && !isCallBackExit) {
            bot.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callback.getId())
                    .text("Please select at least one answer first.")
                    .showAlert(false)
                    .build());
            return;
        }

        int timeTakenMs = (int) (System.currentTimeMillis() - state.getQuestionSentAt());

        SessionQuestion sq = sessionService.getCurrentQuestion(telegramId);

        boolean correct = evaluationService.evaluate(
                telegramId, state.getSelectedChoiceIds(), timeTakenMs);

        // Show inline result on the question message
        String resultText  = ResultFormatter.format(sq, correct, state.getSelectedChoiceIds());

        bot.execute(EditMessageText.builder()
                .chatId(chatId)
                .messageId(state.getActiveMessageId())
                .text(resultText)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());

        state.resetSelection();

        // Advance the session
        SessionService.AdvanceResult result;

        if (isCallBackExit) {
            sessionService.completeSession(telegramId);
            result = SessionService.AdvanceResult.SESSION_COMPLETE;
        } else {
            result = sessionService.advance(telegramId);
        }


        if (result == SessionService.AdvanceResult.NEXT_QUESTION) {
            SessionQuestion next = sessionService.getCurrentQuestion(telegramId);
            questionSender.send(bot, chatId, telegramId, next);

        } else {
            boolean isQuizFullyCompleted = sq.getSession().getTotalQuestions() == (sq.getSequenceOrder() + 1);

            // Session complete — show statistics
            SessionStatsDto stats = statisticsService.buildForLastSession(telegramId);
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(StatisticsFormatter.format(stats, isQuizFullyCompleted))
                    .parseMode("HTML")
                    .build());
        }
    }
}