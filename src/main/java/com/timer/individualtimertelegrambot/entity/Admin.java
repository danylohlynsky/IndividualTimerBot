package com.timer.individualtimertelegrambot.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NonNull
    private Long chatId;

    @Column
    @NonNull
    private Long userId;
}
