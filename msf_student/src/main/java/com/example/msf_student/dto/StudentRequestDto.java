package com.example.msf_student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Request payload DTO for creating or updating a student in msf_student.
 */
public class StudentRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nama is required")
    private String nama;

    @NotNull(message = "Umur is required")
    private Integer umur;

    @NotBlank(message = "Kelas is required")
    private String kelas;

    @NotBlank(message = "Jenis kelas is required")
    private String jenisKelas;

    public StudentRequestDto() {
    }

    public StudentRequestDto(String nama, Integer umur, String kelas, String jenisKelas) {
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
