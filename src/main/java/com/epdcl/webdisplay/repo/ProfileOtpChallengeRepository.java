package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.ProfileOtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface ProfileOtpChallengeRepository
        extends JpaRepository<ProfileOtpChallenge, Long> {

    Optional<ProfileOtpChallenge> findByChallengeId(String challengeId);

    void deleteByUserIdAndPurposeAndChannel(
            Long userId, String purpose, String channel);

    Optional<ProfileOtpChallenge>
    findFirstByUserIdAndPurposeAndChannelOrderByIdDesc(
            Long userId, String purpose, String channel);
}