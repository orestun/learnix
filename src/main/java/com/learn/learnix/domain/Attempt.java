package com.learn.learnix.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attempts")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_question_id", nullable = false)
    private SessionQuestion sessionQuestion;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    /**
     * How long the user took to answer, in milliseconds.
     * Measured from when the question was sent to when the callback arrived.
     */
    @Column(name = "time_taken_ms")
    private Integer timeTakenMs;

    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    @ManyToMany
    @JoinTable(
            name = "attempt_choices",
            joinColumns = @JoinColumn(name = "attempt_id"),
            inverseJoinColumns = @JoinColumn(name = "choice_id")
    )
    @Builder.Default
    private Set<Choice> selectedChoices = new HashSet<>();

    @PrePersist
    private void prePersist() {
        answeredAt = LocalDateTime.now();
    }
}
