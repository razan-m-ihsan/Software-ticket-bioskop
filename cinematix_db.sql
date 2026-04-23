-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 13, 2026 at 09:15 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `cinematix_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `movies`
--

CREATE TABLE `movies` (
  `id` int(11) NOT NULL,
  `judul` varchar(150) NOT NULL,
  `sinopsis` text DEFAULT NULL,
  `durasi` int(11) NOT NULL COMMENT 'Durasi dalam menit',
  `genre` varchar(100) DEFAULT NULL,
  `rating_usia` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `movies`
--

INSERT INTO `movies` (`id`, `judul`, `sinopsis`, `durasi`, `genre`, `rating_usia`) VALUES
(2, 'Avengers', 'Superhero Marvel', 120, 'Action', '13+'),
(6, 'El Camino: A Breaking Bad Movie', 'After a dramatic escape from captivity, Jesse Pinkman must deal with his past in order to make some kind of future for himself.', 122, 'Thriller', '13+');

-- --------------------------------------------------------

--
-- Table structure for table `schedules`
--

CREATE TABLE `schedules` (
  `id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `studio_id` int(11) NOT NULL,
  `tanggal_tayang` date NOT NULL,
  `jam_tayang` time NOT NULL,
  `harga` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `schedules`
--

INSERT INTO `schedules` (`id`, `movie_id`, `studio_id`, `tanggal_tayang`, `jam_tayang`, `harga`) VALUES
(1, 2, 2, '2026-04-10', '19:00:00', 50000.00);

-- --------------------------------------------------------

--
-- Table structure for table `studios`
--

CREATE TABLE `studios` (
  `id` int(11) NOT NULL,
  `nama_studio` varchar(50) NOT NULL,
  `kapasitas` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `studios`
--

INSERT INTO `studios` (`id`, `nama_studio`, `kapasitas`) VALUES
(1, 'Studio 1', 50),
(2, 'Studio 2', 40),
(3, 'Studio 3', 30);

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `schedule_id` int(11) NOT NULL,
  `nomor_kursi` varchar(10) NOT NULL,
  `total_harga` decimal(10,2) NOT NULL,
  `metode_pembayaran` varchar(50) NOT NULL,
  `status` enum('Pending','Success','Failed') DEFAULT 'Pending',
  `waktu_transaksi` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`id`, `user_id`, `schedule_id`, `nomor_kursi`, `total_harga`, `metode_pembayaran`, `status`, `waktu_transaksi`) VALUES
(1, 1, 1, 'A5', 50000.00, 'QRIS', 'Success', '2026-04-10 04:55:58'),
(2, 1, 1, 'A4', 50000.00, 'QRIS', 'Success', '2026-04-10 04:55:58'),
(3, 1, 1, 'A6', 50000.00, 'QRIS', 'Success', '2026-04-10 04:56:12'),
(4, 1, 1, 'A7', 50000.00, 'QRIS', 'Success', '2026-04-10 04:56:12'),
(5, 1, 1, 'A8', 50000.00, 'QRIS', 'Success', '2026-04-10 04:57:04'),
(6, 1, 1, 'A7', 50000.00, 'QRIS', 'Success', '2026-04-10 04:57:04'),
(7, 1, 1, 'A3', 50000.00, 'QRIS', 'Success', '2026-04-10 05:01:16'),
(8, 1, 1, 'A2', 50000.00, 'QRIS', 'Success', '2026-04-10 05:01:16'),
(9, 1, 1, 'A1', 50000.00, 'QRIS', 'Success', '2026-04-10 05:01:16'),
(10, 1, 1, 'B6', 50000.00, 'QRIS', 'Success', '2026-04-10 10:23:32'),
(11, 1, 1, 'B5', 50000.00, 'QRIS', 'Success', '2026-04-10 10:23:32'),
(12, 1, 1, 'B4', 50000.00, 'QRIS', 'Success', '2026-04-10 10:23:32'),
(13, 1, 1, 'B7', 50000.00, 'QRIS', 'Success', '2026-04-10 10:23:32'),
(14, 1, 1, 'B8', 50000.00, 'QRIS', 'Success', '2026-04-10 10:27:50'),
(15, 1, 1, 'C8', 50000.00, 'QRIS', 'Success', '2026-04-10 10:27:50'),
(16, 2, 1, 'B2', 50000.00, 'QRIS', 'Success', '2026-04-10 10:48:49'),
(17, 2, 1, 'B1', 50000.00, 'QRIS', 'Success', '2026-04-10 10:48:49'),
(18, 2, 1, 'B3', 50000.00, 'QRIS', 'Success', '2026-04-10 11:38:22'),
(19, 2, 1, 'C3', 50000.00, 'QRIS', 'Success', '2026-04-10 11:38:22'),
(20, 2, 1, 'C4', 50000.00, 'QRIS', 'Success', '2026-04-10 11:38:22'),
(21, 2, 1, 'C2', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:11'),
(22, 2, 1, 'C1', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:11'),
(23, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:24'),
(24, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:25'),
(25, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:25'),
(26, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:26'),
(27, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:26'),
(28, 2, 1, 'C5', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:42'),
(29, 2, 1, 'C6', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:50'),
(30, 2, 1, 'C7', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:50'),
(31, 2, 1, 'C6', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(32, 2, 1, 'C7', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(33, 2, 1, 'C6', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(34, 2, 1, 'C7', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(35, 2, 1, 'C6', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(36, 2, 1, 'C7', 50000.00, 'QRIS', 'Success', '2026-04-10 12:59:51'),
(37, 2, 1, 'C6', 50000.00, 'QRIS', 'Success', '2026-04-10 13:00:04'),
(38, 2, 1, 'C7', 50000.00, 'QRIS', 'Success', '2026-04-10 13:00:04');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('Admin','User') NOT NULL DEFAULT 'User'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nama`, `email`, `password`, `role`) VALUES
(1, 'Admin Ganteng', 'admin@cinematix.com', 'admin123', 'Admin'),
(2, 'User Biasa', 'user@gmail.com', '123', 'User');

-- --------------------------------------------------------

--
-- Table structure for table `promos`
--

CREATE TABLE `promos` (
  `id` int(11) NOT NULL,
  `kode_promo` varchar(50) NOT NULL,
  `deskripsi` text DEFAULT NULL,
  `diskon_persen` decimal(5,2) DEFAULT 0.00,
  `diskon_rupiah` decimal(10,2) DEFAULT 0.00,
  `tanggal_mulai` date NOT NULL,
  `tanggal_akhir` date NOT NULL,
  `aktif` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promos`
--

INSERT INTO `promos` (`id`, `kode_promo`, `deskripsi`, `diskon_persen`, `diskon_rupiah`, `tanggal_mulai`, `tanggal_akhir`, `aktif`) VALUES
(1, 'DISKON10', 'Diskon 10% untuk semua film', 10.00, 0.00, '2026-04-01', '2026-12-31', 1),
(2, 'HEMAT5000', 'Hemat Rp 5.000 untuk tiket premium', 0.00, 5000.00, '2026-04-01', '2026-12-31', 1),
(3, 'MOVIENIGHT', 'Diskon 15% untuk tayangan malam hari', 15.00, 0.00, '2026-04-15', '2026-04-30', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `movies`
--
ALTER TABLE `movies`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `schedules`
--
ALTER TABLE `schedules`
  ADD PRIMARY KEY (`id`),
  ADD KEY `movie_id` (`movie_id`),
  ADD KEY `studio_id` (`studio_id`);

--
-- Indexes for table `studios`
--
ALTER TABLE `studios`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `schedule_id` (`schedule_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `promos`
--
ALTER TABLE `promos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `kode_promo` (`kode_promo`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `movies`
--
ALTER TABLE `movies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `schedules`
--
ALTER TABLE `schedules`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `studios`
--
ALTER TABLE `studios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `promos`
--
ALTER TABLE `promos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `schedules`
--
ALTER TABLE `schedules`
  ADD CONSTRAINT `schedules_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `schedules_ibfk_2` FOREIGN KEY (`studio_id`) REFERENCES `studios` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
