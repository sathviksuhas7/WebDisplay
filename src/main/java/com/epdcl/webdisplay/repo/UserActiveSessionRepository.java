package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.UserActiveSession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActiveSessionRepository
        extends JpaRepository<UserActiveSession, Long> {

    Optional<UserActiveSession> findByUserId(Long userId);
}