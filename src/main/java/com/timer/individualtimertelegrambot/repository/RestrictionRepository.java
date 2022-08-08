package com.timer.individualtimertelegrambot.repository;

import com.timer.individualtimertelegrambot.entity.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {
}
