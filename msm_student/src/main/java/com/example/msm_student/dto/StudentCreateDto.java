package com.example.msm_student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Request DTO for creating a new Student.
 */
public class StudentCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nama siswa is required")
    private String nama;

    @NotNull(message = "Umur siswa is required")
    @Min(value = 1, message = "Umur must be at least 1")
    @Max(value = 150, message = "Umur cannot exceed 150")
    private Integer umur;

    @NotBlank(message = "Kelas is required")
    private String kelas;

    @NotBlank(message = "Jenis kelas is required")
    private String jenisKelas;

    public StudentCreateDto() {
    }

    public StudentCreateDto(String nama, Integer umur, String kelas, String jenisKelas) {
        this.nama = nama;
        this.umur = umur;
        this.kelas = kelas;
        this.jenisKelas = jenisKelas;
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
}
