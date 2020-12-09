CREATE DATABASE IF NOT EXISTS `nim` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use nim;

#用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(40) NOT NULL default '',
  `pwd` varchar(40) NOT NULL default '',
  `uuid` int NOT NULL,
  `registTime` TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#全局的重要数据 数据表
CREATE TABLE IF NOT EXISTS `sys` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `uuid` bigint NOT NULL default 100
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO sys(uuid) values(100);

#朋友关系表
CREATE TABLE IF NOT EXISTS `friend` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `friend_id` bigint NOT NULL,
  `status` int default 0,
  `friend_info` varchar(300),
  `user_info` varchar(300)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#status字段 1 双向普通好友 2 user是friend的单向好友 3 friend是user的单向好友 4 假删除好友 5 拉黑
#friend_info字段 user为friend添加的备注/分组/背景图片 以json方式保存 插入数据时 Math.min(user_id, friend_id)在后 需要注意顺序

#加入的群表
CREATE TABLE IF NOT EXISTS `group_map` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `group_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#群关系表
CREATE TABLE IF NOT EXISTS `group_info` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '所有者uuid',
  `members` longtext COMMENT '群成员json',
  `groupName` varchar(255)  NOT NULL DEFAULT '' COMMENT '群名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#好友/群存储
#https://blog.csdn.net/php_xml/article/details/108690219
