package com.example.server.user;

import com.example.server.entity.FriendEntity;
import com.example.server.entity.GroupInfoEntity;
import com.example.server.entity.UserCheckEntity;
import com.example.server.utils.auth.AuthUtil;
import com.example.server.utils.auth.PassToken;
import org.apache.commons.lang.StringUtils;
import com.example.server.entity.UserEntity;
import com.example.server.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.BaseEntity;
import utils.Constant;
import utils.L;
import utils.UUIDUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @PassToken
    @RequestMapping(value = "/login")
    public BaseEntity<UserCheckEntity> login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd
            , @RequestParam(value = "deviceType") int deviceType) {
        UserEntity userEntity = new UserEntity();
        userEntity.name = name;
        userEntity.pwd = pwd;
        UserCheckEntity res = userService.login(userEntity);
        boolean loginSuccess = res != null && StringUtils.isNotEmpty(res.uuid);
        if (loginSuccess) {
            res.serviceList = Constant.SERVER_LIST;
            res.token = AuthUtil.createToken(res.uuid);
        }

        return loginSuccess ? BaseEntity.succ(res) : BaseEntity.fail();
    }

    @RequestMapping(value = "/logout")
    public String logout(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @PassToken
    @RequestMapping(value = "/register")
    @ResponseBody
    public BaseEntity register(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        UserEntity userEntity = new UserEntity();
        userEntity.name = name;
        userEntity.pwd = pwd;
        userEntity.uuid = UUIDUtil.getUid();
        userEntity.registerTime = new Date();
        int res = userService.register(userEntity);
        return res == 1 ? BaseEntity.succ() : BaseEntity.fail();
    }

    @RequestMapping(value = "/getAllFriend")
    @ResponseBody
    public BaseEntity<List<FriendEntity>> getAllFriend(HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        L.e("getAttribute==>" + uuid);
        List<FriendEntity> res = userService.getAllFriend(uuid);
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/getAllGroup")
    @ResponseBody
    public BaseEntity<List<GroupInfoEntity>> getAllGroup(HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        List<GroupInfoEntity> res = userService.getAllGroup(uuid);
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/createGroup")
    @ResponseBody
    public BaseEntity<GroupInfoEntity> createGroup(@RequestParam(value = "groupName") String groupName, HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        GroupInfoEntity groupEntity = new GroupInfoEntity();
        groupEntity.groupId = UUIDUtil.getUid();
        groupEntity.uuid = uuid;
        groupEntity.name = groupName;
        groupEntity.memberCount = 1;
        int res = userService.createGroup(groupEntity);
        return res == 1 ? BaseEntity.succ(groupEntity) : BaseEntity.fail();
    }
}
