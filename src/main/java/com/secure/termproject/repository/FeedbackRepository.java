package com.secure.termproject.repository;


import com.secure.termproject.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // no additional methods needed atm
}
