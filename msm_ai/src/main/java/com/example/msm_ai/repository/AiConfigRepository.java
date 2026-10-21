package com.example.msm_ai.repository;

import com.example.msm_ai.model.AiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiConfigRepository extends JpaRepository<AiConfig, UUID> {
    Optional<AiConfig> findByConfigKey(String configKey);
}
