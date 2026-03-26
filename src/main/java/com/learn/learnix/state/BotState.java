package com.learn.learnix.state;

public enum BotState {

    // User sent /start or finished a quiz. Bot is waiting for any command.
    IDLE,

    // Bot sent the topic keyboard. Waiting for a topic callback.
    SELECTING_TOPIC,

    // Bot sent the sub-topic keyboard. Waiting for a sub-topic callback.
    SELECTING_SUB_TOPIC,

    CHOOSING_QUESTION_SET_QUANTITY,

    // Quiz is running. Waiting for choice toggles or a submit callback.
    IN_QUIZ,

    // Bot just showed the answer result. Waiting for "next question" or
    // the session to auto-advance.
    SHOWING_RESULT
}