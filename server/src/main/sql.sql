CREATE DATABASE IF NOT EXISTS `nim` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use nim;

#用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `nick_name` varchar(40) NOT NULL default '' COMMENT '用户名',
  `pwd` varchar(40) NOT NULL default '',
  `uuid` char(36) NOT NULL COMMENT '用户唯一id 做实际的业务',
  `alsa` varchar(64) NOT NULL COMMENT '用户唯一id 可修改',
  `status` int NOT NULL COMMENT '账号状态 封禁等',
  `friend_policy` int NOT NULL COMMENT '账号策略 是否可查找、可加好友等',
  `sex` int(4) COMMENT '0 保密 1 男 2 女 3 男女 4 女男',
  `head_img` varchar(255) COMMENT '头像url',
  `friend_group` varchar(2000) COMMENT '好友分组 格式:{1:好友, 2:...}',
  `phone` varchar(16)  DEFAULT '' COMMENT '绑定手机',
  `email` varchar(255)  DEFAULT '' COMMENT '绑定邮箱',
  `region` varchar(255)  DEFAULT '' COMMENT '地址',
  `autograph` varchar(255)  DEFAULT '' COMMENT '个性签名',
  `birthday` TIMESTAMP COMMENT '生日',
  `py_initial` char(1) NOT NULL DEFAULT '' COMMENT '首字母',
  `quan_pin` varchar(255) NOT NULL DEFAULT '' COMMENT '全拼',
  `regist_time` TIMESTAMP NOT NULL COMMENT '注册时间',
  `last_login_time` TIMESTAMP NOT NULL COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#朋友关系表 比较uuid 小的为user 大的为friend
CREATE TABLE IF NOT EXISTS `friend` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `user_id_min` char(36) NOT NULL COMMENT '用户id',
  `friend_id_max` char(36) NOT NULL COMMENT '用户id',
  `status` int default 0 COMMENT '好友关系',
  `friend_asla` varchar(64) COMMENT '备注',
  `friend_group_id` varchar(64) COMMENT '分组id',
  `friend_back_img` varchar(64) COMMENT '聊天背景图片',
  `friend_status` varchar(64) COMMENT '特别关心等状态',
  `user_asla` varchar(64) COMMENT '备注',
  `user_group_id` varchar(64) COMMENT '分组id',
  `user_back_img` varchar(64) COMMENT '聊天背景图片',
  `user_status` varchar(64) COMMENT '特别关心等状态',
  `add_from` int COMMENT '添加方式',
  `add_time` TIMESTAMP NOT NULL COMMENT '加好友时间'
  `del_time` TIMESTAMP comment '删除好友时间',
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#status字段 1 双向普通好友 2 user是friend的单向好友 3 friend是user的单向好友 4 假删除好友 5 拉黑

#加入的群表
CREATE TABLE IF NOT EXISTS `group_map` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `user_id` char(36)  NOT NULL,
  `group_id` char(36)  NOT NULL,
  `level` int default 0 COMMENT '群等级',
  `alsa` varchar(64) NOT NULL COMMENT '群备注名',
  `role` int default 0 COMMENT '在群中的角色 管理等',
  `nick_name` int default 0 COMMENT '用户备注名',
  `user_group_id` varchar(64) COMMENT '分组id',
  `msg_policy` int default 0 COMMENT '接收群消息策略 如接收不提示等',
  `add_time` TIMESTAMP NOT NULL COMMENT '加群时间',
  `last_update_time` TIMESTAMP COMMENT '最后发言时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#群关系表
CREATE TABLE IF NOT EXISTS `group_info` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `group_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL COMMENT '所有者uuid',
  `head_img` varchar(512)  DEFAULT '' COMMENT '群头像',
  `status` int NOT NULL COMMENT '群状态 封禁等',
  `members` longtext COMMENT '群成员json group_map的外键',
  `group_name` varchar(255)  NOT NULL DEFAULT '' COMMENT '群名称',
  `group_info` varchar(255)  NOT NULL DEFAULT '' COMMENT '群备注',
  `pyInitial` varchar(255)  NOT NULL DEFAULT '' COMMENT '群首字母',
  `quanPin` varchar(255)  NOT NULL DEFAULT '' COMMENT '群全拼'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#好友/群存储
#https://blog.csdn.net/php_xml/article/details/108690219
