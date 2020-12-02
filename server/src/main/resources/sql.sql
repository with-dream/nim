CREATE DATABASE IF NOT EXISTS `nim` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use nim;

CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(40) NOT NULL default '',
  `pwd` varchar(40) NOT NULL default '',
  `uuid` int NOT NULL,
  `registTime` DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
