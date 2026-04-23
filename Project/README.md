## CineTix - Cinema Ticket Booking System

Aplikasi pemesanan tiket bioskop berbasis Java Swing dengan fitur lengkap untuk user dan admin.

### Fitur Utama

- **Login/Register**: Sistem autentikasi untuk user dan admin
- **Dashboard User**: Pemesanan tiket dengan pilihan film, studio, kursi, dan makanan
- **Promo System**: Sistem diskon dan promo untuk penghematan biaya
- **Admin Dashboard**: Manajemen film, jadwal, dan data sistem
- **Pembayaran**: Integrasi QRIS untuk pembayaran

### Fitur Promo Baru

Sistem promo telah ditambahkan dengan fitur:

- **Daftar Promo Aktif**: Menampilkan promo yang sedang berlaku
- **Jenis Diskon**: Diskon persentase (%) atau nominal (Rp)
- **Validasi Tanggal**: Promo hanya aktif dalam periode tertentu
- **Integrasi Pesanan**: Promo dapat diterapkan ke pesanan tiket

#### Struktur Database Promo

```sql
CREATE TABLE promos (
  id INT PRIMARY KEY AUTO_INCREMENT,
  kode_promo VARCHAR(50) UNIQUE NOT NULL,
  deskripsi TEXT,
  diskon_persen DECIMAL(5,2) DEFAULT 0.00,
  diskon_rupiah DECIMAL(10,2) DEFAULT 0.00,
  tanggal_mulai DATE NOT NULL,
  tanggal_akhir DATE NOT NULL,
  aktif BOOLEAN DEFAULT TRUE
);
```

#### Promo Sample

- **DISKON10**: Diskon 10% untuk semua film
- **HEMAT5000**: Hemat Rp 5.000 untuk tiket premium
- **MOVIENIGHT**: Diskon 15% untuk tayangan malam hari

### Cara Menggunakan

1. **Login** sebagai user
2. **Pilih Film** dari dashboard
3. **Pilih Studio & Jam** tayang
4. **Pilih Kursi** yang diinginkan
5. **Tambah Makanan/Minuman** (opsional)
6. **Gunakan Promo** untuk mendapatkan diskon
7. **Lanjut ke Pembayaran**

### Troubleshooting Promo System

Jika promo tidak muncul, ikuti langkah-langkah berikut:

#### 1. Setup Database
```powershell
# Jalankan script setup database
.\setup_database.ps1
```

Atau import manual melalui MySQL:
```sql
mysql -u root -p cinematix_db < setup_database.sql
```

#### 2. Verifikasi Data Promo
```sql
USE cinematix_db;
SELECT * FROM promos;
```

#### 3. Test Koneksi Database
Jalankan test class:
```bash
java -cp "bin;lib/*" main.TestPromo
```

#### 4. Periksa Error Logs
- Pastikan MySQL server berjalan
- Verifikasi kredensial database di `DatabaseConnection.java`
- Cek tanggal sistem (promo aktif berdasarkan CURRENT_DATE)

#### 5. Debug Mode
Saat ini PromoPage menampilkan semua promo (termasuk yang tidak aktif) untuk debugging. Setelah masalah teratasi, kembalikan ke `getActivePromos()`.

## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
