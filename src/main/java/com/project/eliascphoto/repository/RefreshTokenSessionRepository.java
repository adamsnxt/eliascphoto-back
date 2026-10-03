package com.project.eliascphoto.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.eliascphoto.model.RefreshTokenSession;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, String> {

    @Modifying
    @Query("UPDATE RefreshTokenSession session SET session.revokedAt = :now "
            + "WHERE session.tokenId = :tokenId AND session.userName = :userName "
            + "AND session.revokedAt IS NULL AND session.expiresAt > :now")
    int revokeIfActive(
            @Param("tokenId") String tokenId,
            @Param("userName") String userName,
            @Param("now") Instant now);

    void deleteByExpiresAtBefore(Instant now);
}
