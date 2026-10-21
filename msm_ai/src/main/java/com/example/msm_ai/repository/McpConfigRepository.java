package com.example.msm_ai.repository;

import com.example.msm_ai.model.McpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface McpConfigRepository extends JpaRepository<McpConfig, UUID> {
    List<McpConfig> findAllByOrderByCreatedAtAsc();
    List<McpConfig> findByIsActiveTrue();
}
