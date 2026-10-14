package com.example.msm_user.repository;

import com.example.msm_user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User persistence operations with UUID identifier.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
