# Panduan Lengkap Microservices Ecosystem (MSF & MSM Layer)

Proyek ini dibangun menggunakan **Java 21/24**, **Spring Boot 3.3.4**, dan **PostgreSQL**, dengan arsitektur microservices yang memisahkan tanggung jawab antara:
- **Layer `msf_*` (Microservice Frontend / Orchestration Layer)**: Menangani alur bisnis, autentikasi OAuth2 / JWT, otorisasi role, enkripsi password (BCrypt), zona waktu, orkestrasi pemanggilan service, serta menghasilkan response strongly-typed (`ApiResponse<UserDto>`, `ApiResponse<PagingResult<UserDto>>`, `ApiResponse<StudentDto>`, `ApiResponse<PagingResult<StudentDto>>`).
- **Layer `msm_*` (Microservice Management / Data Persistence Layer)**: Menangani CRUD database PostgreSQL lokal (schema `public`), migrasi Flyway otomatis, serta persistensi entitas dengan identitas **UUID (UID)**.
- **`library_helper` (Shared Library)**: Modul library bersama untuk generic `PagingResult<T>`, JWT token service berbasis RSA-2048 (RS256) menggunakan Nimbus JOSE, BCrypt password encoder, filter korelasi tracing (`X-Correlation-Id`), dan HTTP activity logging filter.

---

## 📋 Daftar Modul, Port & Kategori

Seluruh modul bersifat **mandiri (standalone)**, memiliki file `pom.xml` masing-masing tanpa root `pom.xml`, dan menggunakan folder resource tunggal `src/main/resource` (tidak ada folder `resources`).

| No | Modul | Port | Kategori | Tipe Identitas | Fungsi Utama |
|:---:|---|:---:|---|:---:|---|
| 1 | [`library_helper`](file:///d:/udemy/java-springboot-with-mcp/library_helper) | - | Common Library | N/A | Tipe data generic `PagingResult<T>`, Nimbus JWT RSA-2048, BCrypt config, Web filters |
| 2 | [`msm_user`](file:///d:/udemy/java-springboot-with-mcp/msm_user) | **8081** | Data Layer (CRUD) | UUID | Tabel `users`, Flyway migrasi, CRUD user, query data user |
| 3 | [`msm_student`](file:///d:/udemy/java-springboot-with-mcp/msm_student) | **8082** | Data Layer (CRUD) | UUID | Tabel `students`, Flyway migrasi, CRUD siswa, filter nama siswa |
| 4 | [`msf_auth`](file:///d:/udemy/java-springboot-with-mcp/msf_auth) | **8080** | Orchestration (Auth) | UUID | Sign-Up (BCrypt hashing), Login, penandatanganan JWT dengan RSA-2048 Private Key |
| 5 | [`msf_user`](file:///d:/udemy/java-springboot-with-mcp/msf_user) | **8083** | Orchestration (User) | UUID | Create user (Role `super_admin` only, zona waktu Bangkok), user paging |
| 6 | [`msf_student`](file:///d:/udemy/java-springboot-with-mcp/msf_student) | **8084** | Orchestration (Student) | UUID | Forward Bearer token ke `msm_student`, paging siswa dengan filter nama |

---

## 🛠️ Prasyarat Sebelum Menjalankan

Sebelum menjalankan aplikasi, pastikan environment berikut sudah tersedia di komputer Anda:
1. **Java Development Kit (JDK)**: JDK 21 atau JDK 24 (periksa dengan `java -version`).
2. **Apache Maven**: Versi 3.9+ (periksa dengan `mvn -version`).
3. **Database PostgreSQL**:
   - Host: `localhost`
   - Port: `5432`
   - Database Name: `postgres`
   - Schema: `public`
   - Username: `postgres`
   - Password: `postgres` *(dapat disesuaikan pada file `application.yml` masing-masing modul bila berbeda)*.

---

## 🚀 Urutan Build & Menjalankan Service

> [!IMPORTANT]
> **Modul `library_helper` WAJIB di-build dan di-install terlebih dahulu** ke local repository Maven (`.m2`) sebelum modul-modul lain di-build, karena modul `msm_*` dan `msf_*` bergantung langsung padanya.

### Langkah 1: Build & Install `library_helper`
Buka terminal dan jalankan:
```powershell
cd d:\udemy\java-springboot-with-mcp\library_helper
mvn clean install
```
*Tunggu hingga muncul pesan `BUILD SUCCESS`. Library kini terpasang di `~/.m2/repository/com/example/library_helper/1.0.0-SNAPSHOT`.*

---

### Langkah 2: Build & Jalankan Layer MSM (Data Layer)

Layer MSM perlu dijalankan terlebih dahulu agar layer MSF dapat terhubung ke endpoint data.

#### 1. Modul `msm_user` (Port 8081)
Buka **Terminal 1**:
```powershell
# 1. Masuk ke direktori msm_user
cd d:\udemy\java-springboot-with-mcp\msm_user

# 2. Lakukan compile & build
mvn clean install

# 3. Jalankan service Spring Boot
mvn spring-boot:run
```
*Log sukses:* `Tomcat started on port 8081 (http)` dan Flyway menjalankan script migrasi tabel `users`.

#### 2. Modul `msm_student` (Port 8082)
Buka **Terminal 2**:
```powershell
# 1. Masuk ke direktori msm_student
cd d:\udemy\java-springboot-with-mcp\msm_student

# 2. Lakukan compile & build
mvn clean install

# 3. Jalankan service Spring Boot
mvn spring-boot:run
```
*Log sukses:* `Tomcat started on port 8082 (http)` dan Flyway menjalankan script migrasi tabel `students`.

---

### Langkah 3: Build & Jalankan Layer MSF (Orchestration Layer)

#### 1. Modul `msf_auth` (Port 8080)
Buka **Terminal 3**:
```powershell
# 1. Masuk ke direktori msf_auth
cd d:\udemy\java-springboot-with-mcp\msf_auth

# 2. Lakukan compile & build
mvn clean install

# 3. Jalankan service Spring Boot
mvn spring-boot:run
```
*Log sukses:* `Tomcat started on port 8080 (http)`.

#### 2. Modul `msf_user` (Port 8083)
Buka **Terminal 4**:
```powershell
# 1. Masuk ke direktori msf_user
cd d:\udemy\java-springboot-with-mcp\msf_user

# 2. Lakukan compile & build
mvn clean install

# 3. Jalankan service Spring Boot
mvn spring-boot:run
```
*Log sukses:* `Tomcat started on port 8083 (http)`.

#### 3. Modul `msf_student` (Port 8084)
Buka **Terminal 5**:
```powershell
# 1. Masuk ke direktori msf_student
cd d:\udemy\java-springboot-with-mcp\msf_student

# 2. Lakukan compile & build
mvn clean install

# 3. Jalankan service Spring Boot
mvn spring-boot:run
```
*Log sukses:* `Tomcat started on port 8084 (http)`.

---

## 📡 Panduan Lengkap Skenario Pengujian API (cURL)

Berikut adalah urutan pengujian lengkap dari pendaftaran pengguna, login untuk memperoleh token Bearer RSA-2048, pembuatan data oleh `super_admin`, hingga paginasi data siswa.

### Skenario 1: Sign-Up Pengguna Baru melalui `msf_auth`
Password mentah dienkripsi dengan BCrypt di layer `msf_auth` sebelum data pengguna diteruskan ke `msm_user`.
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "marcellius",
    "password": "password123",
    "role": "super_admin"
  }'
```

---

### Skenario 2: Login untuk Mendapatkan Bearer Token (RSA-2048)
`msf_auth` memvalidasi kredensial pengguna ke `msm_user` dan menerbitkan token JWT bertanda tangan digital RSA-2048:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "marcellius@example.com",
    "password": "password123"
  }'
```
*(Catatan: Anda juga dapat menggunakan akun seed bawaan migrasi `loginId`: `marcellius@example.com` / `password`: `password123`)*

**Contoh Response Login:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "expiresIn": 86400,
    "loginId": "marcellius@example.com",
    "name": "marcellius",
    "roles": ["super_admin"]
  }
}
```

Simpan token ke dalam variabel terminal:
- **Pada PowerShell:**
  ```powershell
  $TOKEN = "eyJhbGciOiJSUzI1NiIs..."
  ```
- **Pada Git Bash / Linux / macOS:**
  ```bash
  export TOKEN="eyJhbGciOiJSUzI1NiIs..."
  ```

---

### Skenario 3: Buat User Baru melalui `msf_user` (Wajib Role `super_admin`)
Pada endpoint ini:
- Caller wajib menyertakan token `super_admin`.
- Request body berisi format: `{"name": "...", "loginId": "...", "password": "..."}`.
- Service `msf_user` secara otomatis mengambil `loginId` caller sebagai `createdBy`.
- Service `msf_user` secara otomatis mengambil waktu saat ini dengan zona waktu **Asia/Bangkok** sebagai `createdDate`.
- Password dienkripsi dengan BCrypt sebelum diteruskan ke `msm_user`.
- Mengembalikan objek strongly-typed `ApiResponse<UserDto>`.

**Request:**
```bash
curl -X POST http://localhost:8083/api/msf_user/create/newUser \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "john_doe",
    "loginId": "john_doe@example.com",
    "password": "password123"
  }'
```

**Response (`ApiResponse<UserDto>`):**
```json
{
  "success": true,
  "message": "User created successfully by super_admin",
  "data": {
    "id": "e4f8d224-b153-4f9d-8f2c-e1654e58b991",
    "loginId": "john_doe@example.com",
    "name": "john_doe",
    "role": "pengguna",
    "active": true,
    "createdBy": "marcellius@example.com",
    "createdDate": "2026-10-14T17:27:27",
    "createdAt": "2026-10-14T17:27:27",
    "updatedAt": "2026-10-14T17:27:27"
  },
  "timestamp": "2026-10-14T17:27:27"
}
```

---

### Skenario 4: Paginasi Data User melalui `msf_user`
Mengembalikan strongly-typed `ApiResponse<PagingResult<UserDto>>`:
```bash
curl -X GET "http://localhost:8083/api/msf_user/getDataUser/paging?page=0&rows=10&sortBy=id&sortDir=asc" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Skenario 5: Tambah Siswa Baru melalui `msf_student`
`msf_student` meneruskan request ke `msm_student` dengan membawa Bearer token untuk verifikasi autentikasi:
```bash
curl -X POST http://localhost:8084/api/msf_student/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nama": "Marcellius Tan",
    "umur": 18,
    "kelas": "12-IPA-1",
    "jenisKelas": "Reguler"
  }'
```

**Response (`ApiResponse<StudentDto>`):**
```json
{
  "success": true,
  "message": "Student created successfully",
  "data": {
    "id": "75bef270-ae5a-43e6-b9cd-e14add5e3ac0",
    "nama": "Marcellius Tan",
    "umur": 18,
    "kelas": "12-IPA-1",
    "jenisKelas": "Reguler",
    "createdAt": "2026-10-14T17:28:39",
    "updatedAt": "2026-10-14T17:28:39"
  },
  "timestamp": "2026-10-14T17:28:39"
}
```

---

### Skenario 6: Paginasi Siswa dengan Filter Nama melalui `msf_student`
`msf_student` memanggil `msm_student` dengan parameter filtering dan meneruskan Bearer token untuk integritas data. Mengembalikan `ApiResponse<PagingResult<StudentDto>>`.
```bash
curl -X GET "http://localhost:8084/api/msf_student/getDataStudent/paging?page=0&rows=10&filters.name.value=marcellius&filters.name.matchMode=contains" \
  -H "Authorization: Bearer $TOKEN"
```

**Response (`ApiResponse<PagingResult<StudentDto>>`):**
```json
{
  "success": true,
  "message": "Students retrieved successfully",
  "data": {
    "data": [
      {
        "id": "75bef270-ae5a-43e6-b9cd-e14add5e3ac0",
        "nama": "Marcellius Tan",
        "umur": 18,
        "kelas": "12-IPA-1",
        "jenisKelas": "Reguler",
        "createdAt": "2026-10-14T17:28:39",
        "updatedAt": "2026-10-14T17:28:39"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false
  },
  "timestamp": "2026-10-14T17:28:40"
}
```

---

### Skenario 7: Ambil Detail Siswa Berdasarkan UUID melalui `msf_student`
```bash
curl -X GET "http://localhost:8084/api/msf_student/75bef270-ae5a-43e6-b9cd-e14add5e3ac0" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Skenario 8: Update Data Siswa melalui `msf_student`
```bash
curl -X PUT "http://localhost:8084/api/msf_student/75bef270-ae5a-43e6-b9cd-e14add5e3ac0" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nama": "Marcellius Tan Update",
    "umur": 19,
    "kelas": "12-IPA-1",
    "jenisKelas": "Akselerasi"
  }'
```

---

### Skenario 9: Hapus Siswa melalui `msf_student`
```bash
curl -X DELETE "http://localhost:8084/api/msf_student/75bef270-ae5a-43e6-b9cd-e14add5e3ac0" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔒 Catatan Keamanan Private Key RSA-2048

Untuk mencegah kebocoran private key ke Git repository:
1. File `application.yml` di semua modul **tidak menyimpan hardcoded private key**.
2. **Local Development (Default)**: Layanan Spring Boot secara dinamis men-generate ephemeral key pair RSA-2048 in-memory saat booting.
3. **Production Deployment**: Masukkan private key via environment variable:
   ```powershell
   $env:RSA_PRIVATE_KEY_PATH = "C:\secure\keys\rsa_private.pem"
   $env:RSA_PUBLIC_KEY_PATH = "C:\secure\keys\rsa_public.pem"
   ```
   atau string PEM langsung melalui `RSA_PRIVATE_KEY`.

---

## ❓ FAQ & Panduan Mengatasi Kendala (Troubleshooting)

1. **Error `Connection refused: connect` saat MSF memanggil MSM**:
   - Pastikan modul layer MSM (`msm_user` port 8081 dan `msm_student` port 8082) sudah dalam status *running* sebelum melakukan pengujian melalui layer MSF.
2. **Error `Connection to localhost:5432 refused`**:
   - Pastikan PostgreSQL service di komputer lokal sudah berjalan dan database `postgres` siap menerima koneksi.
3. **Error `Could not resolve dependencies ... library_helper`**:
   - Jalankan `mvn clean install` di folder `library_helper` terlebih dahulu sebelum meng-compile modul lainnya.
4. **Error `Port already in use`**:
   - Pastikan port `8080`, `8081`, `8082`, `8083`, dan `8084` tidak sedang dipakai oleh proses lain (dapat diperiksa menggunakan perintah PowerShell: `Get-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess`).
