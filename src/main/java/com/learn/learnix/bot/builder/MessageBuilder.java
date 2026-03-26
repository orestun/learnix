package com.learn.learnix.bot.builder;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageBuilder {

    public static EditMessageText buildEditMessageText(Long chatId, Integer messageId, String text) {
        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .parseMode("Markdown")
                .build();
    }

    public static EditMessageReplyMarkup buildEditMessageReplyMarkup(Long chatId, Integer messageId, InlineKeyboardMarkup keyboard) {
        return EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboard)
                .build();
    }
}
