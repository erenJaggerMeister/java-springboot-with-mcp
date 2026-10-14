package com.example.msm_student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * Request DTO for updating an existing Student.
 */
public class StudentUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nama;

    @Min(value = 1, message = "Umur must be at least 1")
    @Max(value = 150, message = "Umur cannot exceed 150")
    private Integer umur;

    private String kelas;

    private String jenisKelas;

    public StudentUpdateDto() {
    }

    public StudentUpdateDto(String nama, Integer umur, String kelas, String jenisKelas) {
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
