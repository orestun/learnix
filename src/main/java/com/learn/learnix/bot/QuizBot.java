package com.learn.learnix.bot;

import com.learn.learnix.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class QuizBot extends TelegramLongPollingBot {

    private final UpdateDispatcher dispatcher;
    private final BotConfig config;

    public QuizBot(BotConfig config, UpdateDispatcher dispatcher, BotConfig config1) {
        super(config.getToken());
        this.dispatcher = dispatcher;
        this.config = config1;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername(); // injected via super if needed
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            dispatcher.dispatch(this, update);
        } catch (Exception e) {
            log.error("Unhandled error processing update id={}",
                    update.getUpdateId(), e);
        }
    }
}