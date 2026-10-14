package com.example.msm_student.repository;

import com.example.msm_student.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Student persistence operations with UUID key.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findByKelas(String kelas);

    List<Student> findByJenisKelas(String jenisKelas);

    Page<Student> findAll(Pageable pageable);

    Page<Student> findByNamaContainingIgnoreCase(String nama, Pageable pageable);
}
