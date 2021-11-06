package com.example.server.controller;

import com.example.server.entity.*;
import com.example.server.redis.RConst;
import com.example.server.service.GroupService;
import com.example.server.service.UserService;
import com.example.server.utils.auth.AuthUtil;
import com.example.server.utils.auth.PassToken;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.BaseEntity;
import utils.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 *
 * 二、群成员
 * 1 群列表
 * 2 加群
 * 2 申请加群
 * 3 退群
 * 4 修改群名片
 * 5 群成员列表
 * 三、群主
 * 1 创建群
 * 2 解散群
 * 3 设置群管理
 * 4 取消群管理
 * 5 全员禁言
 * 四、群管理
 * 1 剔除成员
 * 2 禁言
 * 3 加群请求处理
 * */

@RestController
@RequestMapping("/group")
public class GroupController {
    @Resource
    GroupService groupService;

    @Resource
    RedissonClient redisson;

    @RequestMapping(value = "/groupList")
    @ResponseBody
    public BaseEntity<List<GroupInfoEntity>> groupList(HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        List<GroupInfoEntity> res = groupService.groupList(uuid);
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/createGroup")
    @ResponseBody
    @Transactional
    public BaseEntity<GroupInfoEntity> createGroup(@RequestParam(value = "groupName") String groupName, HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        GroupInfoEntity groupEntity = new GroupInfoEntity();
        groupEntity.groupId = UUIDUtil.getUid();
        groupEntity.name = groupName;
        groupEntity.memberCount = 1;
        int res = groupService.createGroup(groupEntity);
        return res == 1 ? BaseEntity.succ(groupEntity) : BaseEntity.fail();
    }
}
