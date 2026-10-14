package com.example.msm_student.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Student JPA Entity using UUID as identifier.
 */
@Entity
@Table(name = "students", schema = "msm_student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "umur", nullable = false)
    private Integer umur;

    @Column(name = "kelas", nullable = false, length = 50)
    private String kelas;

    @Column(name = "jenis_kelas", nullable = false, length = 50)
    private String jenisKelas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Student() {
    }

    public Student(UUID id, String nama, Integer umur, String kelas, String jenisKelas, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nama = nama;
        this.umur = umur;
        this.kelas = kelas;
        this.jenisKelas = jenisKelas;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Integer getUmur() {
        return umur;
    }

    public void setUmur(Integer umur) {
        this.umur = umur;
    }

    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public String getJenisKelas() {
        return jenisKelas;
    }

    public void setJenisKelas(String jenisKelas) {
        this.jenisKelas = jenisKelas;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
