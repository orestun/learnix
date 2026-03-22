package com.learn.learnix.service;

import com.learn.learnix.domain.User;
import com.learn.learnix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Registers a new user on first /start, or silently returns the existing one.
     * Called on every /start so it must be idempotent.
     */
    @Transactional
    public User getOrRegister(Long telegramId, String username) {
        return userRepository.findByTelegramId(telegramId)
                .map(existing -> {
                    // Keep username in sync if it changed in Telegram
                    if (!existing.getUsername().equals(username)) {
                        existing.setUsername(username);
                        log.debug("Updated username for telegramId={}", telegramId);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .build();
                    log.info("Registered new user telegramId={} username={}", telegramId, username);
                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public User getByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException(
                        "User not found for telegramId=" + telegramId));
    }
}
