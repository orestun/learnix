package com.learn.learnix.bot;

import com.learn.learnix.bot.handler.QuestionHandler;
import com.learn.learnix.bot.handler.StartHandler;
import com.learn.learnix.bot.handler.TopicHandler;
import com.learn.learnix.state.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final StartHandler startHandler;
    private final TopicHandler topicHandler;
    private final QuestionHandler questionHandler;
    private final UserStateManager stateManager;

    public void dispatch(AbsSender bot, Update update) throws Exception {

        // ── Text commands ──────────────────────────────────────────────────
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text      = update.getMessage().getText();
            Long   telegramId = update.getMessage().getFrom().getId();
            Long   chatId     = update.getMessage().getChatId();

            // /start always wins — abandon any in-progress session and reset
            if ("/start".equals(text) || "/restart".equals(text)) {
                startHandler.handle(bot, telegramId, chatId);
                return;
            }

            // Ignore unexpected text during a quiz
            log.debug("Ignoring text '{}' in state {} for telegramId={}",
                    text, stateManager.getBotState(telegramId), telegramId);
            return;
        }

        // ── Callback queries (button taps) ─────────────────────────────────
        if (update.hasCallbackQuery()) {
            Long   telegramId = update.getCallbackQuery().getFrom().getId();
            BotState state     = stateManager.getBotState(telegramId);

            switch (state) {
                case SELECTING_TOPIC ->
                        topicHandler.handle(bot, update.getCallbackQuery());
                case IN_QUIZ ->
                        questionHandler.handle(bot, update.getCallbackQuery());
                default ->
                        log.warn("Received callback in unexpected state={} for telegramId={}",
                                state, telegramId);
            }
        }
    }
}
