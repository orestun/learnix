package com.learn.learnix.config;

import com.learn.learnix.bot.QuizBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BotRegistrationConfig {

    private final QuizBot quizBot;

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(quizBot);
            log.info("QuizBot registered successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to register QuizBot", e);
            throw new RuntimeException(e);
        }
    }
}