package com.example.server.user;

import com.example.server.entity.FriendModel;
import com.example.server.entity.GroupInfoModel;
import com.example.server.entity.UserCheckModel;
import org.apache.commons.lang.StringUtils;
import com.example.server.entity.UserModel;
import com.example.server.service.UserService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.BaseModel;
import utils.Constant;
import utils.L;
import utils.UUIDUtil;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @Resource
    UuidManager uuidManager;

    @Resource
    private AmqpTemplate rabbit;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping(value = "/test")
    public String test() {
        rabbit.convertAndSend("aaa", "aaaaaa");
        return "==>";
    }

    @RequestMapping(value = "/login")
    public BaseModel<UserCheckModel> login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd
            , @RequestParam(value = "deviceType") int deviceType) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        UserCheckModel res = userService.login(userModel);
        boolean loginSuccess = res != null && StringUtils.isNotEmpty(res.uuid);
        if (loginSuccess) {
            res.serviceList = Constant.SERVER_LIST;
        }

        return loginSuccess ? BaseModel.succ(res) : BaseModel.fail();
    }

    @RequestMapping(value = "/unlogin")
    public String unlogin(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @RequestMapping(value = "/register")
    @ResponseBody
    public BaseModel register(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        userModel.uuid = UUIDUtil.getUid();
        userModel.registerTime = new Date();
        int res = userService.register(userModel);
        return res == 1 ? BaseModel.succ() : BaseModel.fail();
    }

    @RequestMapping(value = "/getAllFriend")
    @ResponseBody
    public BaseModel<List<FriendModel>> getAllFriend(@RequestParam(value = "uuid") String uuid) {
        List<FriendModel> res = userService.getAllFriend(uuid);
        return BaseModel.succ(res);
    }

    @RequestMapping(value = "/getAllGroup")
    @ResponseBody
    public BaseModel<List<GroupInfoModel>> getAllGroup(@RequestParam(value = "uuid") String uuid) {
        List<GroupInfoModel> res = userService.getAllGroup(uuid);
        return BaseModel.succ(res);
    }

    @RequestMapping(value = "/createGroup")
    @ResponseBody
    public BaseModel<GroupInfoModel> createGroup(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "groupName") String groupName) {
        GroupInfoModel groupModel = new GroupInfoModel();
        groupModel.groupId = UUIDUtil.getUid();
        groupModel.uuid = uuid;
        groupModel.name = groupName;
        groupModel.memberCount = 1;
        int res = userService.createGroup(groupModel);
        return res == 1 ? BaseModel.succ(groupModel) : BaseModel.fail();
    }
}
