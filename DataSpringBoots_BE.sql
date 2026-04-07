CREATE DATABASE  IF NOT EXISTS `webkhoahocon` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `webkhoahocon`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: webkhoahocon
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `CategoryID` int NOT NULL AUTO_INCREMENT,
  `CategoryName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ParentID` int DEFAULT NULL,
  PRIMARY KEY (`CategoryID`),
  UNIQUE KEY `CategoryName` (`CategoryName`),
  KEY `CategoryID_idx` (`ParentID`),
  CONSTRAINT `CategoryID` FOREIGN KEY (`ParentID`) REFERENCES `categories` (`CategoryID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Toán học',1),(2,'Anh văn',1),(3,'Văn học',1),(4,'Thiết kế',1),(6,'Nấu ăn',1),(7,'Lập trình',1);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `CourseID` int NOT NULL AUTO_INCREMENT,
  `CourseName` varchar(225) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `CategoryID` int NOT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `CreatedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  `imag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`CourseID`),
  KEY `FKahnttdwp5gm73pp06e839u3qd` (`CategoryID`),
  CONSTRAINT `FKahnttdwp5gm73pp06e839u3qd` FOREIGN KEY (`CategoryID`) REFERENCES `categories` (`CategoryID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (4,'IELTS Reading','tự học IELTS Reading hiệu quả tại nhà',2,100000.00,'2025-04-07 05:22:00','src/assets/img/IELTS.png'),(5,'Môn Văn lớp 9','Nội dung thuộc dự án Văn học - Yêu lại từ đầu',3,200000.00,'2025-04-07 12:10:07','src/assets/img/Van-sang-tao.png'),(7,'Toán nâng cao lớp 5','Toán nâng cao lớp 5 - So sánh phân số',1,120000.00,'2025-04-08 14:25:04','src/assets/img/Toan-nang-cao.png'),(8,'Giải đề thi Violympic cấp quốc gia','Toán nâng cao, bồi dưỡng học sinh giỏi lớp 4',1,100000.00,'2025-04-09 08:51:47','src/assets/img/Toan-nang-cao.png'),(9,'HỌC VẼ CƠ BẢN','CHÚC TẤT CẢ MỌI NGƯỜI LUÔN CẢM THẤY VUI KHI HỌC VẼ VÀ TIẾN BỘ MỖI NGÀY!',4,150000.00,'2025-10-25 22:02:17','src/assets/img/Hoc-ve-co-ban.png');
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment` (
  `EnrollmentID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `CourseID` int NOT NULL,
  `EnrollmentDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CompletionStatus` varchar(45) DEFAULT NULL,
  `CurrentLessonID` int DEFAULT NULL,
  PRIMARY KEY (`EnrollmentID`),
  KEY `CourseID_idx` (`CourseID`),
  KEY `FK_Enroll_CurrentLesson` (`CurrentLessonID`),
  KEY `FKihkd6m7r3jrxlhlm8bc99mmht` (`UserID`),
  CONSTRAINT `CourseID` FOREIGN KEY (`CourseID`) REFERENCES `courses` (`CourseID`),
  CONSTRAINT `FK_Enroll_CurrentLesson` FOREIGN KEY (`CurrentLessonID`) REFERENCES `lessons` (`LessonID`) ON DELETE SET NULL,
  CONSTRAINT `FKihkd6m7r3jrxlhlm8bc99mmht` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `enrollment_chk_1` CHECK ((`CompletionStatus` in (_utf8mb4'Not Started',_utf8mb4'In Progress',_utf8mb4'Completed')))
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment`
--

LOCK TABLES `enrollment` WRITE;
/*!40000 ALTER TABLE `enrollment` DISABLE KEYS */;
INSERT INTO `enrollment` VALUES (39,21,5,'2026-03-19 22:48:59','Not Started',NULL),(40,21,7,'2026-03-19 22:48:59','Not Started',NULL),(41,21,8,'2026-03-19 22:48:59','Not Started',NULL),(42,42,5,'2026-04-01 10:49:01','Not Started',NULL),(43,42,4,'2026-04-02 14:55:19','Not Started',NULL);
/*!40000 ALTER TABLE `enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lesson_progress`
--

DROP TABLE IF EXISTS `lesson_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_progress` (
  `ProgressID` int NOT NULL AUTO_INCREMENT,
  `EnrollmentID` int NOT NULL,
  `LessonID` int NOT NULL,
  `WatchedPercentage` decimal(5,2) DEFAULT '0.00',
  `IsCompleted` tinyint(1) DEFAULT '0',
  `LastWatchedAt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ProgressID`),
  KEY `idx_progress_enrollment` (`EnrollmentID`),
  KEY `idx_progress_lesson` (`LessonID`),
  CONSTRAINT `FK_Progress_Enrollment` FOREIGN KEY (`EnrollmentID`) REFERENCES `enrollment` (`EnrollmentID`) ON DELETE CASCADE,
  CONSTRAINT `FK_Progress_Lesson` FOREIGN KEY (`LessonID`) REFERENCES `lessons` (`LessonID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lesson_progress`
--

LOCK TABLES `lesson_progress` WRITE;
/*!40000 ALTER TABLE `lesson_progress` DISABLE KEYS */;
INSERT INTO `lesson_progress` VALUES (9,39,3,100.00,1,'2026-04-01 11:34:14'),(10,39,4,9.93,0,'2026-04-01 11:34:21');
/*!40000 ALTER TABLE `lesson_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lessons`
--

DROP TABLE IF EXISTS `lessons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lessons` (
  `LessonID` int NOT NULL AUTO_INCREMENT,
  `CourseID` int NOT NULL,
  `LessonTitle` varchar(255) NOT NULL,
  `videourl` varchar(255) NOT NULL,
  `Duration` int DEFAULT NULL,
  `OrderIndex` int DEFAULT '1',
  `CreatedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`LessonID`),
  KEY `idx_lessons_course` (`CourseID`),
  CONSTRAINT `FK_Lesson_Course` FOREIGN KEY (`CourseID`) REFERENCES `courses` (`CourseID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lessons`
--

LOCK TABLES `lessons` WRITE;
/*!40000 ALTER TABLE `lessons` DISABLE KEYS */;
INSERT INTO `lessons` VALUES (1,4,'Bài 1: Giới thiệu kỹ năng Reading','IELTS_Reading_B1.mp4',808,1,'2025-10-17 08:03:22'),(2,4,'Bài 2: Cách phân tích đề Reading','IELTS_Reading_B2.mp4',763,2,'2025-10-17 08:03:22'),(3,5,'Bài 1: Giới thiệu văn học lớp 9','VanLop9_B1.mp4',947,1,'2025-10-17 08:03:22'),(4,5,'Bài 2: Phân tích Truyện Kiều','VanLop9_B2.mp4',512,2,'2025-10-17 08:03:22'),(5,7,'Bài 1: So sánh phân số','ToanLop5_B1.mp4',2315,1,'2025-10-17 08:03:22'),(6,7,'Bài 2: Quy đồng mẫu số','ToanLop5_B2.mp4',3144,2,'2025-10-17 08:03:22'),(7,7,'Bài 3: Phép cộng phân số','ToanLop5_B1.mp4',2315,3,'2025-10-17 08:03:22'),(8,8,'Bài 1: Giới thiệu đề thi Violympic','Violympic_B1.mp4',2315,1,'2025-10-17 08:03:22'),(9,8,'Bài 2: Giải đề số 1','Violympic_B2.mp4',1407,2,'2025-10-17 08:03:22'),(10,9,'Bài 1: Chuẩn bị dụng cụ và Những bài tập đầu tiên','HocVeCoBan_B1.mp4',2118,1,'2025-10-25 22:11:09'),(11,9,'Bài 2: Phối cảnh điểm tụ (1-2-3)','HocVeCoBan_B2.mp4',1531,2,'2025-10-25 22:12:42'),(12,9,'Bài 3: Phương pháp dựng hình (Phần 1 - Hình)','HocVeCoBan_B3.mp4',1233,3,'2025-10-26 13:10:58'),(13,9,'Bài 4: Phương pháp dựng hình (Phần 2 - Khối cơ bản và ứng dụng)','HocVeCoBan_B4.mp4',1690,4,'2025-10-26 13:10:58'),(14,9,'Bài 5: Ứng dụng Vẽ Phối Cảnh 1 Điểm Tụ:Điểm Tụ nằm ngoài tranh& những trường hợp khác','HocVeCoBan_B5.mp4',1715,5,'2025-10-26 13:10:58');
/*!40000 ALTER TABLE `lessons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_details`
--

DROP TABLE IF EXISTS `order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_details` (
  `OrderDetailID` int NOT NULL AUTO_INCREMENT,
  `OrderID` int NOT NULL,
  `CourseID` int NOT NULL,
  PRIMARY KEY (`OrderDetailID`),
  KEY `OrderID` (`OrderID`),
  KEY `CourseID` (`CourseID`),
  CONSTRAINT `order_details_ibfk_1` FOREIGN KEY (`OrderID`) REFERENCES `orders` (`OrderID`),
  CONSTRAINT `order_details_ibfk_2` FOREIGN KEY (`CourseID`) REFERENCES `courses` (`CourseID`)
) ENGINE=InnoDB AUTO_INCREMENT=131 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_details`
--

LOCK TABLES `order_details` WRITE;
/*!40000 ALTER TABLE `order_details` DISABLE KEYS */;
INSERT INTO `order_details` VALUES (123,121,5),(124,121,7),(125,121,8),(128,124,5),(129,125,4),(130,126,7);
/*!40000 ALTER TABLE `order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `OrderID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `OrderDate` datetime DEFAULT CURRENT_TIMESTAMP,
  `TotalAmount` decimal(10,2) DEFAULT NULL,
  `Status` enum('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
  `notes` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`OrderID`),
  KEY `FKpnm1eeupqm4tykds7k3okqegv` (`UserID`),
  CONSTRAINT `FKpnm1eeupqm4tykds7k3okqegv` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (121,21,'2026-03-19 22:48:12',420000.00,'SUCCESS','provider=MOMO;momoOrderId=MOMO_121_b3301cc250d8;requestId=MOMO_121_b3301cc250d8;status=SUCCESS;resultCode=0;transId=4705750989',NULL,NULL),(124,42,'2026-04-01 10:49:01',200000.00,'SUCCESS','Thanh toan MoMo',NULL,NULL),(125,42,'2026-04-01 11:20:22',100000.00,'SUCCESS','',NULL,NULL),(126,42,'2026-04-03 19:22:55',120000.00,'PENDING','provider=MOMO;momoOrderId=MOMO_126_7621344df50d;requestId=MOMO_126_7621344df50d;status=PENDING',NULL,NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `PermissionID` int NOT NULL AUTO_INCREMENT,
  `PermissionName` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PermissionID`),
  UNIQUE KEY `PermissionName_UNIQUE` (`PermissionName`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'QUẢN LÝ HỌC VIÊN','Quản lý học viên'),(2,'QUẢN LÝ KHÓA HỌC','Quyền quản lý khóa học'),(3,'QUẢN LÝ ĐƠN HÀNG','Quyền quản lý đơn hàng');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `RolePermissionID` int NOT NULL AUTO_INCREMENT,
  `RoleID` int NOT NULL,
  `PermissionID` int NOT NULL,
  PRIMARY KEY (`RolePermissionID`),
  KEY `FKf1nn7cski78ixjcxu20d6idln` (`PermissionID`),
  KEY `FKs110n3si8bjv4rg22v6s0sqk2` (`RoleID`),
  CONSTRAINT `FKf1nn7cski78ixjcxu20d6idln` FOREIGN KEY (`PermissionID`) REFERENCES `permissions` (`PermissionID`),
  CONSTRAINT `FKs110n3si8bjv4rg22v6s0sqk2` FOREIGN KEY (`RoleID`) REFERENCES `roles` (`RoleID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES (1,1,1),(2,1,2),(3,1,3);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `RoleID` int NOT NULL AUTO_INCREMENT,
  `RoleName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`RoleID`),
  UNIQUE KEY `RoleName_UNIQUE` (`RoleName`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Admin'),(2,'Student'),(3,'Teacher');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `UserRoleID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `RoleID` int NOT NULL,
  PRIMARY KEY (`UserRoleID`),
  KEY `FKss07htsrasc17qsq2o9422nyh` (`RoleID`),
  CONSTRAINT `FKss07htsrasc17qsq2o9422nyh` FOREIGN KEY (`RoleID`) REFERENCES `roles` (`RoleID`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1,1),(4,4,2),(5,5,2),(6,6,2),(7,7,2),(8,8,2),(10,20,2),(11,21,2),(26,42,2);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `UserName` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `CreateTime` datetime DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  CONSTRAINT `chk_status` CHECK ((`status` in (_utf8mb4'Hoạt động',_utf8mb4'Bị khóa')))
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin','$2a$10$tzTwXA3AiFRnHaLifbNNruZavgHKROHZBXk3Q.6wVbNWVxO352kBm','Admin@gmail.com','Hoạt động',NULL),(6,'tri','$2b$08$aBV2xWSM8mskDG6vNzL2CumCmhCbJCh2UXejnlty0JQ6iGbsJg1cC','john2025@gmail.com','Hoạt động','2025-04-09 00:28:47'),(7,'tien','$2b$08$v2S7hetCZP6.QQDcd4G/6eErDYJzeQexQJG.2gkDC4rSme2HuSQTm','tien123@gmail.com','Hoạt động','2025-04-09 08:26:52'),(8,'tri123','$2b$08$CyBEl9VLZbR6K2tSQAQKA.8W03YCdkz6ncbMudYQKphEJRTMQ8zVe','tri123@gmail.com','Hoạt động','2025-04-09 08:54:00'),(14,'tri16102004','$2a$10$UA81kI9kLk22053lZqXrlu711I5OZEvDMPkUQhY6Lbi4x4.MTLr5i','trantriltk3@gmail.com','Hoạt động','2026-03-06 17:12:20'),(20,'student01','$2a$10$4.UJ/sSFYb.sP4owgGO5t.XNEncCfbzk6qMiFUTF7q/8xCsR6wgqO','student01@example.com','Hoạt động','2026-03-15 16:58:45'),(21,'test2','$2a$10$sDeGSMK56ooRWoKH/8Vi0eMxWEvcucRlWCnB9TD7diHABv0MILPxi','test2@gmail.com','Hoạt động','2026-03-15 17:03:23'),(42,'test3','$2b$10$luD/Amd1GJgXCPTGUYptKOxfSxa950E9owuN/Rby9Ew6Yoh28hOaW','test3@gmail.com','Hoat dong','2026-04-01 10:36:50');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist`
--

DROP TABLE IF EXISTS `wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist` (
  `WishlistID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `CourseID` int NOT NULL,
  `CreatedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`WishlistID`),
  KEY `FKif0m1a0twfsjbvummoxok71j5` (`CourseID`),
  KEY `FKiw03lkof0if6cc2gcjr0jtpvx` (`UserID`),
  CONSTRAINT `FKif0m1a0twfsjbvummoxok71j5` FOREIGN KEY (`CourseID`) REFERENCES `courses` (`CourseID`),
  CONSTRAINT `FKiw03lkof0if6cc2gcjr0jtpvx` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist`
--

LOCK TABLES `wishlist` WRITE;
/*!40000 ALTER TABLE `wishlist` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-04  9:30:02
