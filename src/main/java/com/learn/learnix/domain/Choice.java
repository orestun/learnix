package com.learn.learnix.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choices")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    /**
     * Controls the display order of choices in the Telegram inline keyboard.
     * Populated at creation time; can be randomized by the service layer.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}