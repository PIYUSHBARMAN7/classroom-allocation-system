package com.classroomallocation.backend.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import com.classroomallocation.backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.classroomallocation.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailForUpdate(
            @Param("email") String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(
            @Param("id") Long id
    );
    @Query("""
        select u
        from User u
        where u.role = :role
          and u.enabled = true
          and not exists (
              select t.id
              from Teacher t
              where t.user = u
          )
        """)
Page<User> findAvailableTeacherAccounts(
        @Param("role") Role role,
        Pageable pageable
);
}