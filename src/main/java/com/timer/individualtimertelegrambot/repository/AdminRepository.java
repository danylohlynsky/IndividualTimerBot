package com.timer.individualtimertelegrambot.repository;

import com.timer.individualtimertelegrambot.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    List<Admin> getAllByChatId(Long chatId);
}
