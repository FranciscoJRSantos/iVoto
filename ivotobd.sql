-- MySQL dump 10.13  Distrib 5.7.20, for osx10.13 (x86_64)
--
-- Host: localhost    Database: ivotobd
-- ------------------------------------------------------
-- Server version	5.7.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Departamento`
--

DROP TABLE IF EXISTS `Departamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Departamento` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(64) NOT NULL,
  `faculdade_id` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `faculdade_id` (`faculdade_id`),
  CONSTRAINT `departamento_ibfk_1` FOREIGN KEY (`faculdade_id`) REFERENCES `Faculdade` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Departamento`
--

LOCK TABLES `Departamento` WRITE;
/*!40000 ALTER TABLE `Departamento` DISABLE KEYS */;
INSERT INTO `Departamento` VALUES (1,'DEI',1),(3,'DEQ',1);
/*!40000 ALTER TABLE `Departamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Departamento_Eleicao`
--

DROP TABLE IF EXISTS `Departamento_Eleicao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Departamento_Eleicao` (
  `departamento_id` int(11) NOT NULL,
  `eleicao_id` int(11) NOT NULL,
  KEY `departamento_id` (`departamento_id`),
  KEY `eleicao_id` (`eleicao_id`),
  CONSTRAINT `departamento_eleicao_ibfk_1` FOREIGN KEY (`departamento_id`) REFERENCES `Departamento` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `departamento_eleicao_ibfk_2` FOREIGN KEY (`eleicao_id`) REFERENCES `Eleicao` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Departamento_Eleicao`
--

LOCK TABLES `Departamento_Eleicao` WRITE;
/*!40000 ALTER TABLE `Departamento_Eleicao` DISABLE KEYS */;
INSERT INTO `Departamento_Eleicao` VALUES (1,22),(1,23),(1,24);
/*!40000 ALTER TABLE `Departamento_Eleicao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Eleicao`
--

DROP TABLE IF EXISTS `Eleicao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Eleicao` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `titulo` varchar(50) NOT NULL,
  `descricao` varchar(512) NOT NULL,
  `tipo` int(1) NOT NULL,
  `inicio` datetime(6) NOT NULL,
  `fim` datetime(6) NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Eleicao`
--

LOCK TABLES `Eleicao` WRITE;
/*!40000 ALTER TABLE `Eleicao` DISABLE KEYS */;
INSERT INTO `Eleicao` VALUES (22,'OI','BYE',1,'2017-02-20 08:00:00.000000','2017-02-20 20:00:00.000000',0),(23,'NEI/AAC','Nucleo de Estudantes de Informática',1,'2020-02-20 08:00:00.000000','2020-02-20 20:00:00.000000',0),(24,'nem sei','nem seiii',1,'1111-11-11 11:11:00.000000','1111-12-11 11:11:00.000000',0);
/*!40000 ALTER TABLE `Eleicao` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER after_eleicao_create 
    AFTER INSERT ON Eleicao
    FOR EACH ROW 
BEGIN
  INSERT INTO Lista (nome,tipo,votos,eleicao_id) VALUES ("Blank",0,0,new.ID);
  INSERT INTO Lista (nome,tipo,votos,eleicao_id) VALUES ("Null",0,0,new.ID);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `Faculdade`
--

DROP TABLE IF EXISTS `Faculdade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Faculdade` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(20) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Faculdade`
--

LOCK TABLES `Faculdade` WRITE;
/*!40000 ALTER TABLE `Faculdade` DISABLE KEYS */;
INSERT INTO `Faculdade` VALUES (1,'FCTUC'),(2,'FDUC');
/*!40000 ALTER TABLE `Faculdade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Faculdade_Eleicao`
--

DROP TABLE IF EXISTS `Faculdade_Eleicao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Faculdade_Eleicao` (
  `faculdade_id` int(11) NOT NULL,
  `eleicao_id` int(11) NOT NULL,
  KEY `faculdade_id` (`faculdade_id`),
  KEY `eleicao_id` (`eleicao_id`),
  CONSTRAINT `faculdade_eleicao_ibfk_1` FOREIGN KEY (`faculdade_id`) REFERENCES `Faculdade` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `faculdade_eleicao_ibfk_2` FOREIGN KEY (`eleicao_id`) REFERENCES `Eleicao` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Faculdade_Eleicao`
--

LOCK TABLES `Faculdade_Eleicao` WRITE;
/*!40000 ALTER TABLE `Faculdade_Eleicao` DISABLE KEYS */;
/*!40000 ALTER TABLE `Faculdade_Eleicao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Lista`
--

DROP TABLE IF EXISTS `Lista`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Lista` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(20) DEFAULT NULL,
  `tipo` int(1) DEFAULT NULL,
  `votos` int(11) DEFAULT NULL,
  `eleicao_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `eleicao_id` (`eleicao_id`),
  CONSTRAINT `lista_ibfk_1` FOREIGN KEY (`eleicao_id`) REFERENCES `Eleicao` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Lista`
--

LOCK TABLES `Lista` WRITE;
/*!40000 ALTER TABLE `Lista` DISABLE KEYS */;
INSERT INTO `Lista` VALUES (42,'Blank',0,3,22),(43,'Null',0,1,22),(44,'Lista J',1,NULL,NULL),(45,'Blank',0,3,23),(46,'Null',0,1,23),(47,'Lista T',1,NULL,NULL),(48,'Lista A',1,NULL,23),(49,'Blank',0,0,24),(50,'Null',0,0,24);
/*!40000 ALTER TABLE `Lista` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MesaVoto`
--

DROP TABLE IF EXISTS `MesaVoto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MesaVoto` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `departamento_id` int(11) NOT NULL,
  `eleicao_id` int(11) NOT NULL,
  `numeroVotos` int(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `departamento_id` (`departamento_id`),
  KEY `eleicao_id` (`eleicao_id`),
  CONSTRAINT `mesavoto_ibfk_1` FOREIGN KEY (`departamento_id`) REFERENCES `Departamento` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `mesavoto_ibfk_2` FOREIGN KEY (`eleicao_id`) REFERENCES `Eleicao` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MesaVoto`
--

LOCK TABLES `MesaVoto` WRITE;
/*!40000 ALTER TABLE `MesaVoto` DISABLE KEYS */;
INSERT INTO `MesaVoto` VALUES (6,0,1,23,1);
/*!40000 ALTER TABLE `MesaVoto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `hashed_password` varchar(50) NOT NULL,
  `contacto` int(9) NOT NULL,
  `morada` varchar(20) NOT NULL,
  `numeroCC` int(9) NOT NULL,
  `validadeCC` date NOT NULL,
  `role` int(1) NOT NULL,
  `departamento_id` int(11) NOT NULL,
  `faculdade_id` int(11) NOT NULL,
  `lista_id` int(11) DEFAULT NULL,
  `mesavoto_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `numeroCC` (`numeroCC`),
  KEY `departamento_id` (`departamento_id`),
  KEY `faculdade_id` (`faculdade_id`),
  KEY `lista_id` (`lista_id`),
  KEY `mesavoto_id` (`mesavoto_id`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`departamento_id`) REFERENCES `Departamento` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `user_ibfk_2` FOREIGN KEY (`faculdade_id`) REFERENCES `Faculdade` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `user_ibfk_3` FOREIGN KEY (`lista_id`) REFERENCES `Lista` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `user_ibfk_4` FOREIGN KEY (`mesavoto_id`) REFERENCES `MesaVoto` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (5,'John Doe','secret',123456789,'Soure',87654321,'2020-02-20',3,1,1,NULL,NULL),(6,'Xico','secret',918433131,'Matas',14789471,'2017-02-20',1,1,1,NULL,6),(7,'zeze','greger',111111111,'Putas',987654321,'2017-11-11',1,1,1,NULL,NULL);
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User_Eleicao`
--

DROP TABLE IF EXISTS `User_Eleicao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User_Eleicao` (
  `hasVoted` tinyint(1) DEFAULT '1',
  `whenVoted` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `eleicao_id` int(11) NOT NULL,
  `mesavoto_id` int(11) DEFAULT NULL,
  KEY `user_id` (`user_id`),
  KEY `eleicao_id` (`eleicao_id`),
  KEY `mesavoto_id` (`mesavoto_id`),
  CONSTRAINT `user_eleicao_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `user_eleicao_ibfk_2` FOREIGN KEY (`eleicao_id`) REFERENCES `Eleicao` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `user_eleicao_ibfk_3` FOREIGN KEY (`mesavoto_id`) REFERENCES `MesaVoto` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User_Eleicao`
--

LOCK TABLES `User_Eleicao` WRITE;
/*!40000 ALTER TABLE `User_Eleicao` DISABLE KEYS */;
INSERT INTO `User_Eleicao` VALUES (1,'2017-10-28 10:58:00',6,23,6),(1,'2017-10-28 20:11:39',7,23,NULL);
/*!40000 ALTER TABLE `User_Eleicao` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-10-28 22:42:25
