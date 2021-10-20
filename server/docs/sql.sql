CREATE TABLE `friend_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `userId` char(40) NULL,
  `friendId` char(40) NULL,
  `friend` tinyint NULL COMMENT ' 好友状态',
  `block` tinyint NULL COMMENT '拉黑状态',
  `friendInfo` varchar(255) NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `group_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '群名',
  `memberCount` int NULL COMMENT '群成员数量',
  `uuid` char(40) NULL COMMENT '群主',
  `groupId` char(40) NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `group_member`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NULL,
  `groupId` int NOT NULL,
  `role` int NULL COMMENT '角色 1 普通成员 2 管理员 3 群主',
  `level` int NULL COMMENT '等级',
  `insertTime` datetime(0) NULL COMMENT '加入时间',
  `lastTime` datetime(0) NULL COMMENT '最后发言时间',
  PRIMARY KEY (`id`)
);

CREATE TABLE `user_info`  (
  `id` int NOT NULL,
  `name` char(255) NULL,
  `pwd` varchar(255) NULL,
  `uuid` char(40) NULL,
  `registerTime` date NULL,
  PRIMARY KEY (`id`)
);

