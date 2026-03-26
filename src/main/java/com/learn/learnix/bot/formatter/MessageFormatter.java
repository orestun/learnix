package com.learn.learnix.bot.formatter;

public class MessageFormatter {

    public static String formatClarifyMessage(String topicName, String subTopicName, Integer quantity, String text) {
        StringBuilder messageText = new StringBuilder();

        if (topicName != null) {
            messageText.append(String.format("*Topic:* %s", topicName)).append("\n");
        }
        if (subTopicName != null) {
            messageText.append(String.format("*Sub-Topic:* %s", subTopicName)).append("\n");
        }
        if (quantity != null) {
            messageText.append(String.format("*Total questions:* %s", quantity)).append("\n");
        }
        messageText.append("\n");
        messageText.append(text);

        return messageText.toString();
    }
}
