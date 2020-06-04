-- phpMyAdmin SQL Dump
-- version 4.6.6
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Sep 11, 2017 at 02:20 PM
-- Server version: 10.1.20-MariaDB
-- PHP Version: 7.0.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `id11748254_nearcabs`
--

-- --------------------------------------------------------

--
-- Table structure for table `cabs`
--

CREATE TABLE `cabs` (
  `cab_id` int(15) NOT NULL,
  `cab_no` varchar(20) NOT NULL,
  `cab_lat` varchar(10) NOT NULL,
  `cab_lng` varchar(10) NOT NULL,
  `on_trip` int(2) NOT NULL DEFAULT '0',
  `cab_bearing` varchar(30) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `cabs`
--

INSERT INTO `cabs` (`cab_id`, `cab_no`, `cab_lat`, `cab_lng`, `on_trip`, `cab_bearing`) VALUES
(3, 'CH01 5321', '30.7292235', '76.8409035', 1, '0.0');

-- --------------------------------------------------------

--
-- Table structure for table `drivers`
--

CREATE TABLE `drivers` (
  `driver_id` int(15) NOT NULL,
  `one_signal_id` varchar(500) NOT NULL DEFAULT '000',
  `driver_name` varchar(50) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(200) NOT NULL,
  `verification_code` varchar(200) NOT NULL,
  `is_verified` int(2) NOT NULL DEFAULT '0',
  `cab_no` varchar(20) NOT NULL,
  `on_duty` int(2) NOT NULL DEFAULT '1'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `drivers`
--

INSERT INTO `drivers` (`driver_id`, `one_signal_id`, `driver_name`, `phone`, `email`, `password`, `verification_code`, `is_verified`, `cab_no`, `on_duty`) VALUES
(5, 'ac3d663f-6b13-41f4-8668-ee8c8b8bf728', 'Sachin ', '8968894728', 'nams1497@gmail.com', '1234', '$2y$10$ePr/k7cWHQqOcFG3CN/NP.sgpopjgf5qBMnKqjQg7VEo0CnlJimjG', 1, 'CH01 5321', 1);

-- --------------------------------------------------------

--
-- Table structure for table `rides`
--

CREATE TABLE `rides` (
  `ride_id` int(15) NOT NULL,
  `driver_id` int(15) NOT NULL,
  `user_id` int(15) NOT NULL,
  `src_lat` varchar(20) NOT NULL,
  `src_lng` varchar(20) NOT NULL,
  `dest_lat` varchar(20) NOT NULL,
  `dest_lng` varchar(20) NOT NULL,
  `fare` varchar(10) NOT NULL,
  `otp` varchar(10) NOT NULL,
  `status` int(2) NOT NULL DEFAULT '0',
  `booked_at` varchar(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `rides`
--

INSERT INTO `rides` (`ride_id`, `driver_id`, `user_id`, `src_lat`, `src_lng`, `dest_lat`, `dest_lng`, `fare`, `otp`, `status`, `booked_at`) VALUES
(101, 5, 18, '30.730678', '76.841431', '30.7055869', '76.80127089999999', '105', '5970', 1, '1502452252'),
(100, 5, 18, '30.730678', '76.841431', '30.64254959999999', '76.8173359', '202', '2991', 2, '1502451995');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(10) NOT NULL,
  `user_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(150) COLLATE utf8_unicode_ci NOT NULL,
  `verification_code` varchar(150) COLLATE utf8_unicode_ci NOT NULL,
  `is_verified` int(2) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cabs`
--
ALTER TABLE `cabs`
  ADD PRIMARY KEY (`cab_id`),
  ADD UNIQUE KEY `cab_no` (`cab_no`);

--
-- Indexes for table `drivers`
--
ALTER TABLE `drivers`
  ADD PRIMARY KEY (`driver_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`),
  ADD UNIQUE KEY `cab_no` (`cab_no`);

--
-- Indexes for table `rides`
--
ALTER TABLE `rides`
  ADD PRIMARY KEY (`ride_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `cabs`
--
ALTER TABLE `cabs`
  MODIFY `cab_id` int(15) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `drivers`
--
ALTER TABLE `drivers`
  MODIFY `driver_id` int(15) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
--
-- AUTO_INCREMENT for table `rides`
--
ALTER TABLE `rides`
  MODIFY `ride_id` int(15) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=102;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
