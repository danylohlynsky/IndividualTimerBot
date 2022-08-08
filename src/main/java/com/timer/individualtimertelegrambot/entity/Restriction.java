package com.timer.individualtimertelegrambot.entity;

import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "restrictions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Restriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NonNull
    private Long chatId;

    @Column
    @NonNull
    private Long userId;

    @Column
    @NonNull
    private Long secondsLimitation;

    @Column
    private ZonedDateTime lastMessage;
}
