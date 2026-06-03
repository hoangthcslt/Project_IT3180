-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 11, 2026 at 05:35 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bluemoon_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `ho_khau`
--

CREATE TABLE `ho_khau` (
  `id` int(11) NOT NULL,
  `ma_ho_khau` varchar(20) NOT NULL,
  `ten_chu_ho` varchar(100) NOT NULL,
  `dien_tich` decimal(10,2) NOT NULL,
  `ngay_lap` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ho_khau`
--

INSERT INTO `ho_khau` (`id`, `ma_ho_khau`, `ten_chu_ho`, `dien_tich`, `ngay_lap`) VALUES
(2, '3242', 'Tú', 100.00, '2026-05-06');

-- --------------------------------------------------------

--
-- Table structure for table `khoan_thu`
--

CREATE TABLE `khoan_thu` (
  `id` int(11) NOT NULL,
  `ma_khoan_thu` varchar(20) NOT NULL,
  `ten_khoan_thu` varchar(200) NOT NULL,
  `loai_phi` enum('BAT_BUOC','TU_NGUYEN') NOT NULL,
  `don_gia` decimal(15,2) DEFAULT 0.00,
  `ngay_tao` date NOT NULL,
  `ghi_chu` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `khoan_thu`
--

INSERT INTO `khoan_thu` (`id`, `ma_khoan_thu`, `ten_khoan_thu`, `loai_phi`, `don_gia`, `ngay_tao`, `ghi_chu`) VALUES
(1, '123', 'Tiền', 'BAT_BUOC', 500000.00, '2026-05-06', '');

-- --------------------------------------------------------

--
-- Table structure for table `nhan_khau`
--

CREATE TABLE `nhan_khau` (
  `id` int(11) NOT NULL,
  `ho_khau_id` int(11) NOT NULL,
  `ho_ten` varchar(100) NOT NULL,
  `cccd` varchar(12) DEFAULT NULL,
  `ngay_sinh` date NOT NULL,
  `gioi_tinh` enum('NAM','NU','KHAC') NOT NULL,
  `quan_he` varchar(50) NOT NULL,
  `trang_thai` enum('THUONG_TRU','TAM_TRU','TAM_VANG') NOT NULL DEFAULT 'THUONG_TRU'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `nop_tien`
--

CREATE TABLE `nop_tien` (
  `id` int(11) NOT NULL,
  `ho_khau_id` int(11) NOT NULL,
  `khoan_thu_id` int(11) NOT NULL,
  `nguoi_nop` varchar(100) DEFAULT NULL,
  `so_tien_nop` decimal(15,2) DEFAULT NULL,
  `ngay_nop` date DEFAULT NULL,
  `hinh_thuc` enum('TIEN_MAT','CHUYEN_KHOAN') DEFAULT NULL,
  `trang_thai` enum('CHUA_NOP','DA_NOP') NOT NULL DEFAULT 'CHUA_NOP',
  `so_tien_phai_nop` decimal(15,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tam_tru_tam_vang`
--

CREATE TABLE `tam_tru_tam_vang` (
  `id` int(11) NOT NULL,
  `nhan_khau_id` int(11) NOT NULL,
  `loai_khai_bao` enum('TAM_TRU','TAM_VANG') NOT NULL,
  `tu_ngay` date NOT NULL,
  `den_ngay` date NOT NULL,
  `ly_do` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','ACCOUNTANT') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `created_at`) VALUES
(1, 'admin', '$2a$12$epgI30V2yyP9UT4wK2faPu5V.0hydA71p939fwIfOjSGSHlo//0lS', '', '2026-05-06 03:44:27'),
(3, 'Manager', '$2a$12$zyiNDUtfYmuaGdGR3qPLp.vgz.X6LPyBhSAq5NSsNu/yDH35xSaZ6', 'ADMIN', '2026-05-06 03:46:10');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `ho_khau`
--
ALTER TABLE `ho_khau`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ma_ho_khau` (`ma_ho_khau`);

--
-- Indexes for table `khoan_thu`
--
ALTER TABLE `khoan_thu`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ma_khoan_thu` (`ma_khoan_thu`);

--
-- Indexes for table `nhan_khau`
--
ALTER TABLE `nhan_khau`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cccd` (`cccd`),
  ADD KEY `ho_khau_id` (`ho_khau_id`);

--
-- Indexes for table `nop_tien`
--
ALTER TABLE `nop_tien`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ho_khau_id` (`ho_khau_id`),
  ADD KEY `khoan_thu_id` (`khoan_thu_id`);

--
-- Indexes for table `tam_tru_tam_vang`
--
ALTER TABLE `tam_tru_tam_vang`
  ADD PRIMARY KEY (`id`),
  ADD KEY `nhan_khau_id` (`nhan_khau_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `ho_khau`
--
ALTER TABLE `ho_khau`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `khoan_thu`
--
ALTER TABLE `khoan_thu`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `nhan_khau`
--
ALTER TABLE `nhan_khau`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `nop_tien`
--
ALTER TABLE `nop_tien`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `tam_tru_tam_vang`
--
ALTER TABLE `tam_tru_tam_vang`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `nhan_khau`
--
ALTER TABLE `nhan_khau`
  ADD CONSTRAINT `nhan_khau_ibfk_1` FOREIGN KEY (`ho_khau_id`) REFERENCES `ho_khau` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `nop_tien`
--
ALTER TABLE `nop_tien`
  ADD CONSTRAINT `nop_tien_ibfk_1` FOREIGN KEY (`ho_khau_id`) REFERENCES `ho_khau` (`id`),
  ADD CONSTRAINT `nop_tien_ibfk_2` FOREIGN KEY (`khoan_thu_id`) REFERENCES `khoan_thu` (`id`);

--
-- Constraints for table `tam_tru_tam_vang`
--
ALTER TABLE `tam_tru_tam_vang`
  ADD CONSTRAINT `tam_tru_tam_vang_ibfk_1` FOREIGN KEY (`nhan_khau_id`) REFERENCES `nhan_khau` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
