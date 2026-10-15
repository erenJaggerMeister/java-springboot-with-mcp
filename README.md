# Panduan Lengkap Ekosistem Microservices (MFE, MSF, MSM)

Repository ini dibangun menggunakan arsitektur **Microservices & Microfrontends** yang memisahkan tanggung jawab sistem secara ketat ke dalam 3 (tiga) layer utama: **MFE**, **MSF**, dan **MSM**, serta didukung modul library bersama (`library_helper`).

---

## 📌 Ketentuan Arsitektur & Tanggung Jawab Layer

Harap dipahami dan diikuti ketentuan pembagian layer berikut:

1. **MFE (Microservice Frontend)**:
   - Layer yang membuat kode program frontend untuk menampilkan antarmuka pengguna (UI) kepada user.
   - Menggunakan kombinasi **React.js** dan **Vite** dengan prinsip **Microfrontend (Module Federation)**.
   - Seluruh modul diawali dengan penamaan `mfe_{nama modul}`.
2. **MSF (Microservice Federation)**:
   - Layer orkestrasi dan integrasi bisnis yang menerima input dari UI (MFE), mengolah data, memvalidasi otentikasi/otorisasi (JWT Bearer Token RSA-2048), lalu meneruskannya kepada layer MSM.
   - Berfungsi meneruskan output data berdasarkan request kepada layer MSM kembali ke layer MFE.
3. **MSM (Microservice Master)**:
   - Layer persistensi data yang bertugas melakukan CRUD biasa terhadap database (PostgreSQL).
   - **Aturan Ketat:** MSM **dilarang menyebrang** ke modul-modul MSM lainnya (contoh: `msm_user` dilarang memanggil `msm_student`).
   - Bersifat independen dan mengelola skema databasenya masing-masing melalui migrasi otomatis Flyway.
4. **Bebas dari File Test**:
   - Seluruh modul backend di layer MSF dan MSM tidak memiliki ketergantungan atau file pengujian (`src/test`), sehingga perintah `mvn spring-boot:run` dapat dieksekusi secara instan tanpa proses compile/run testing.

---

## 📋 Daftar Modul, Port & Kategori

| No | Modul | Port | Layer | Teknologi | Fungsi Utama |
|:---:|---|:---:|:---:|---|---|
| 1 | [`mfe_main`](file:///d:/udemy/java-springboot-with-mcp/mfe_main) | **3000** | MFE (Host / Shell) | React.js, Vite | Shell container utama, autentikasi login via `msf_auth`, sidebar navigasi, header info profil & role |
| 2 | [`mfe_student`](file:///d:/udemy/java-springboot-with-mcp/mfe_student) | **3001** | MFE (Remote) | React.js, Vite | Remote microfrontend untuk menampilkan data siswa dengan tabel paginasi & sorting |
| 3 | [`mfe_user`](file:///d:/udemy/java-springboot-with-mcp/mfe_user) | **3002** | MFE (Remote) | React.js, Vite | Remote microfrontend untuk menampilkan data pengguna dengan tabel paginasi & sorting |
| 4 | [`msf_auth`](file:///d:/udemy/java-springboot-with-mcp/msf_auth) | **8080** | MSF (Federation) | Spring Boot 3.3.4 | Login, Sign-up, dan penerbitan JWT Bearer Token RSA-2048 |
| 5 | [`msm_user`](file:///d:/udemy/java-springboot-with-mcp/msm_user) | **8081** | MSM (Master CRUD) | Spring Boot 3.3.4, PostgreSQL | CRUD tabel `users`, migrasi Flyway, penyimpanan password BCrypt |
| 6 | [`msm_student`](file:///d:/udemy/java-springboot-with-mcp/msm_student) | **8082** | MSM (Master CRUD) | Spring Boot 3.3.4, PostgreSQL | CRUD tabel `students`, migrasi Flyway, filtering nama siswa |
| 7 | [`msf_user`](file:///d:/udemy/java-springboot-with-mcp/msf_user) | **8083** | MSF (Federation) | Spring Boot 3.3.4 | Orkestrasi user, validasi hak akses Bearer token, paginasi user |
| 8 | [`msf_student`](file:///d:/udemy/java-springboot-with-mcp/msf_student) | **8084** | MSF (Federation) | Spring Boot 3.3.4 | Orkestrasi data siswa, penerusan Bearer token ke `msm_student`, paginasi siswa |
| 9 | [`library_helper`](file:///d:/udemy/java-springboot-with-mcp/library_helper) | - | Shared Library | Java 21, Spring Boot | Utility bersama: `PagingResult<T>`, Nimbus JOSE RSA-2048, Security Filters, Tracing |

---

## 🛠️ Prasyarat Sebelum Menjalankan

Sebelum menjalankan aplikasi, pastikan environment berikut telah terpasang:
1. **Node.js**: Versi 18+ atau 20+ (periksa dengan `node -v` dan `npm -v`).
2. **Java Development Kit (JDK)**: JDK 21 atau JDK 24 (periksa dengan `java -version`).
3. **Apache Maven**: Versi 3.9+ (periksa dengan `mvn -version`).
4. **Database PostgreSQL**:
   - Host: `localhost` | Port: `5432`
   - Database: `postgres` | Schema: `public`
   - Username: `postgres` | Password: `postgres` *(sesuaikan di `application.yml` masing-masing modul jika berbeda)*.

---

## 🚀 Panduan Menjalankan Sistem

### Bagian A: Build & Jalankan Backend (MSM & MSF)

#### 1. Build & Install `library_helper` (Wajib Pertama Kali)
```powershell
cd d:\udemy\java-springboot-with-mcp\library_helper
mvn clean install
```

#### 2. Jalankan Layer MSM (Data Layer)
- **Terminal 1 - `msm_user` (Port 8081):**
  ```powershell
  cd d:\udemy\java-springboot-with-mcp\msm_user
  mvn spring-boot:run
  ```
- **Terminal 2 - `msm_student` (Port 8082):**
  ```powershell
  cd d:\udemy\java-springboot-with-mcp\msm_student
  mvn spring-boot:run
  ```

#### 3. Jalankan Layer MSF (Federation Layer)
- **Terminal 3 - `msf_auth` (Port 8080):**
  ```powershell
  cd d:\udemy\java-springboot-with-mcp\msf_auth
  mvn spring-boot:run
  ```
- **Terminal 4 - `msf_user` (Port 8083):**
  ```powershell
  cd d:\udemy\java-springboot-with-mcp\msf_user
  mvn spring-boot:run
  ```
- **Terminal 5 - `msf_student` (Port 8084):**
  ```powershell
  cd d:\udemy\java-springboot-with-mcp\msf_student
  mvn spring-boot:run
  ```

---

### Bagian B: Build & Jalankan Frontend (MFE Layer)

Modul frontend menggunakan **React.js + Vite** dan **Module Federation** (`@originjs/vite-plugin-federation`). Modul remote (`mfe_student` dan `mfe_user`) di-build dan di-serve agar mengekspos `remoteEntry.js` yang kemudian dikonsumsi secara dinamis oleh wadah host (`mfe_main`).

#### Langkah Cepat (Melalui Root):
1. **Install Dependensi Seluruh MFE:**
   ```powershell
   cd d:\udemy\java-springboot-with-mcp
   npm run install:all
   ```
2. **Build Seluruh Modul MFE:**
   ```powershell
   npm run build:all
   ```
3. **Jalankan Remote `mfe_student` (Port 3001) pada Terminal 6:**
   ```powershell
   npm run start:student
   ```
4. **Jalankan Remote `mfe_user` (Port 3002) pada Terminal 7:**
   ```powershell
   npm run start:user
   ```
5. **Jalankan Host `mfe_main` (Port 3000) pada Terminal 8:**
   ```powershell
   npm run start:main
   ```

Akses browser pada alamat: **`http://localhost:3000`**

---

## 🖥️ Alur Kerja & Fitur Antarmuka Pengguna (Frontend)

### 1. Halaman Autentikasi Login (`mfe_main`)
- Saat pertama kali membuka `http://localhost:3000`, user disajikan form login.
- User memasukkan kredensial:
  - **Login ID**: misal `marcellius@example.com`
  - **Password**: misal `password123`
- Sistem mengirimkan request `POST` ke endpoint `http://localhost:8080/api/v1/auth/login` di layer `msf_auth`.
- **Proteksi Akses**: Jika username atau password tidak valid, sistem menampilkan notifikasi kesalahan dan menolak akses masuk.
- **Login Berhasil**: Token JWT Bearer disimpan di session/local storage beserta data profil user (nama dan daftar role).

### 2. Header & Profil Pengguna
- Di bagian atas aplikasi, terdapat Header bertuliskan:
  > **"Penerapan MCP dengan penggunaan java - springboot"**
- Di sebelah kanan atas header, ditampilkan informasi user yang sedang aktif:
  - **Nama Pengguna** (contoh: `Marcellius`)
  - **Role Pengguna** (contoh: `super_admin`)
  - Tombol **Keluar (Logout)** untuk mengakhiri sesi.

### 3. Navigasi Sidebar Kiri
- Sidebar sebelah kiri memuat navigasi menu:
  - 👨‍🎓 **mfe_student** (`Data Siswa`)
  - 👥 **mfe_user** (`Data Pengguna`)
- Status menu aktif ditandai secara visual, dan user dapat berpindah menu dengan satu kali klik.

### 4. Menu Siswa (`mfe_student`)
- **Alur Awal**: Segera setelah login berhasil, user langsung diarahkan masuk ke menu **mfe_student** terlebih dahulu.
- **Paginasi**: Data siswa ditampilkan dalam bentuk tabel berhalaman (pagination), bukan daftar tanpa batas.
  - User dapat mengatur baris per halaman (5, 10, 20, 50).
  - Terdapat tombol navigasi halaman (Pertama, Sebelumnya, Nomor Halaman, Berikutnya, Terakhir).
- **Integrasi API**: Menghubungi layer MSF dengan Bearer token hasil login:
  ```http
  GET http://localhost:8084/api/msf_student/getDataStudent/paging?page=0&rows=10&sortBy=nama&sortDir=desc
  Authorization: Bearer <TOKEN>
  ```
- **Sorting & Filtering**: Mendukung pengurutan kolom (nama, umur, kelas, tanggal dibuat) dan pencarian berdasarkan nama siswa.

### 5. Menu Pengguna (`mfe_user`)
- Menampilkan daftar data user terdaftar dalam sistem.
- **Paginasi**: Data user disajikan dalam tabel dengan kontrol paginasi lengkap.
- **Integrasi API**: Menghubungi layer MSF dengan Bearer token:
  ```http
  GET http://localhost:8083/api/msf_user/getDataUser/paging?page=0&rows=10&sortBy=id&sortDir=asc
  Authorization: Bearer <TOKEN>
  ```
- Menampilkan kolom: ID (UUID), Nama Pengguna, Login ID, Role (badge), Status Aktif/Nonaktif, dan Tanggal Registrasi.

---

## 📡 Rincian Endpoint API MSF & MSM

### 1. Autentikasi (`msf_auth`)
- **Login:** `POST http://localhost:8080/api/v1/auth/login`
  ```json
  {
    "loginId": "marcellius@example.com",
    "password": "password123"
  }
  ```
- **Sign-Up:** `POST http://localhost:8080/api/v1/auth/signup`
  ```json
  {
    "name": "john_doe",
    "password": "password123",
    "role": "ROLE_USER"
  }
  ```

### 2. Siswa (`msf_student`)
- **Paginasi Siswa:**
  ```bash
  curl -X GET "http://localhost:8084/api/msf_student/getDataStudent/paging?page=0&rows=10&sortBy=nama&sortDir=desc" \
    -H "Authorization: Bearer <TOKEN>"
  ```
- **Detail Siswa:** `GET http://localhost:8084/api/msf_student/{id}`
- **Tambah Siswa:** `POST http://localhost:8084/api/msf_student/create`

### 3. Pengguna (`msf_user`)
- **Paginasi Pengguna:**
  ```bash
  curl -X GET "http://localhost:8083/api/msf_user/getDataUser/paging?page=0&rows=10&sortBy=id&sortDir=asc" \
    -H "Authorization: Bearer <TOKEN>"
  ```
- **Tambah Pengguna Baru (Khusus `super_admin`):** `POST http://localhost:8083/api/msf_user/create/newUser`

---

## ❓ FAQ & Troubleshooting

1. **Bagaimana jika modul `mfe_student` atau `mfe_user` belum dijalankan saat membuka `mfe_main`?**
   - Host `mfe_main` dilengkapi dengan `RemoteErrorBoundary`. Jika salah satu remote belum aktif, host akan menampilkan instruksi command terminal untuk menjalankan modul tersebut beserta tombol *"Coba Sambungkan Ulang"* tanpa membuat aplikasi crash.
2. **Kredensial default untuk pengujian awal?**
   - Database migrasi otomatis menyertakan akun seed:
     - **Username / Login ID:** `marcellius@example.com`
     - **Password:** `password123`
     - **Role:** `super_admin`
3. **Mengapa tidak ada folder `src/test` pada backend?**
   - Sesuai dengan spesifikasi arsitektur, seluruh folder dan dependensi pengujian telah dihapus agar `mvn spring-boot:run` dan proses build Maven berjalan seketika tanpa hambatan kompilasi test.
4. **CORS (Cross-Origin Resource Sharing):**
   - Layer `msf_*` telah dikonfigurasi dengan Spring Security CORS filter untuk mengizinkan request dari domain frontend (`http://localhost:3000`, `http://localhost:3001`, `http://localhost:3002`) serta mendukung preflight request HTTP `OPTIONS`.
