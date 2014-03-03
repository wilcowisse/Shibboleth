-- phpMyAdmin SQL Dump
-- version 3.5.8.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 11, 2014 at 03:31 PM
-- Server version: 5.5.34-0ubuntu0.13.04.1
-- PHP Version: 5.4.9-4ubuntu2.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `shibboleth`
--

-- --------------------------------------------------------

--
-- Table structure for table `Chunks`
--

CREATE TABLE IF NOT EXISTS `Chunks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_id` int(11) NOT NULL,
  `start` int(11) NOT NULL,
  `end` int(11) NOT NULL,
  `committer_id` int(11) NOT NULL,
  `time` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `Committers`
--

CREATE TABLE IF NOT EXISTS `Committers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `repo` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `ContributionInfo`
--

CREATE TABLE IF NOT EXISTS `ContributionInfo` (
  `contribution_id` int(11) NOT NULL,
  `count` int(11) NOT NULL,
  `percentage` int(11) NOT NULL,
  PRIMARY KEY (`contribution_id`),
  UNIQUE KEY `contribution_id` (`contribution_id`),
  KEY `contribution_id_2` (`contribution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Contributions`
--

CREATE TABLE IF NOT EXISTS `Contributions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `repo_name` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=85 ;

-- --------------------------------------------------------

--
-- Table structure for table `Files`
--

CREATE TABLE IF NOT EXISTS `Files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `repo` varchar(255) NOT NULL,
  `head` varchar(40) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `RecordLinks`
--

CREATE TABLE IF NOT EXISTS `RecordLinks` (
  `committer` int(11) NOT NULL,
  `user` varchar(255) NOT NULL,
  PRIMARY KEY (`committer`),
  KEY `committer` (`committer`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Repos`
--

CREATE TABLE IF NOT EXISTS `Repos` (
  `id` int(11) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `owner` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `clone_url` varchar(255) NOT NULL,
  `parent` varchar(255) NOT NULL,
  `fork` tinyint(1) NOT NULL,
  `forks_count` int(11) NOT NULL,
  `size` int(11) NOT NULL,
  `language` varchar(255) NOT NULL,
  PRIMARY KEY (`full_name`),
  UNIQUE KEY `full_name` (`full_name`),
  UNIQUE KEY `id` (`id`),
  KEY `id_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `StoredLinks`
--

CREATE TABLE IF NOT EXISTS `StoredLinks` (
  `name` varchar(255) NOT NULL,
  `type` varchar(64) NOT NULL,
  PRIMARY KEY (`name`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Users`
--

CREATE TABLE IF NOT EXISTS `Users` (
  `id` int(11) NOT NULL,
  `login` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `type` varchar(64) NOT NULL,
  `company` varchar(255) NOT NULL,
  `repos` int(11) NOT NULL,
  `followers` int(11) NOT NULL,
  `following` int(11) NOT NULL,
  PRIMARY KEY (`login`),
  KEY `login` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
