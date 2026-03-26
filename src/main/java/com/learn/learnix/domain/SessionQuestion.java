package com.learn.learnix.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_questions")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 0-based position of this question within the session.
     * Drives which question is shown next.
     */
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @OneToOne(mappedBy = "sessionQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Attempt attempt;

    public boolean isAnswered() {
        return attempt != null;
    }
}
