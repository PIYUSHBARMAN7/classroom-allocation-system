package com.classroomallocation.backend.repository;

import com.classroomallocation.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUser_Id(Long userId);

    @Query("""
            select token.user.id
            from PasswordResetToken token
            where token.tokenHash = :hash
            """)
    Optional<Long> findUserIdByTokenHash(@Param("hash") String hash);
}