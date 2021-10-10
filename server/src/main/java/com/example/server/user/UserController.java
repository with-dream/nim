package com.example.server.user;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.ServerList;
import com.example.server.entity.MQMapModel;
import com.example.server.entity.UserModel;
import com.example.server.entity.UserResultModel;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import netty.MQWrapper;
import netty.model.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.*;
import utils.Constant;
import utils.L;
import utils.UUIDUtil;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    Gson gson = new Gson();

    @Autowired
    UserService userService;

    @Autowired
    UuidManager uuidManager;

    @Autowired
    private AmqpTemplate rabbit;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping(value = "/test")
    public String test() {
        rabbit.convertAndSend("aaa", "aaaaaa");
        return "==>";
    }

    @RequestMapping(value = "/login")
    public String login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd
            , @RequestParam(value = "deviceType") int deviceType) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        UserResultModel result = userService.login(userModel);
        L.p("login res==>" + result);
        if (result == null) {
            result = new UserResultModel();
            result.code = -1;
        } else {
            result.imUrl = ServerList.SERVER_LIST;
            result.clientToken = UUIDUtil.getClientToken();

            long s = System.currentTimeMillis();
            Map<Integer, MQMapModel> map = (Map) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, result.uuid);
            L.e("redis==>" + (System.currentTimeMillis() - s));
            if (map != null && !map.isEmpty()) {
                for (MQMapModel mapModel : map.values()) {
                    if (mapModel.deviceType == deviceType) {
                        //TODO 踢下线操作
                        break;
                    }
                }
            }
        }

        result.code = 0;
        String res = gson.toJson(result);
        System.out.println("==>" + res);

        return res;
    }

    @RequestMapping(value = "/unlogin")
    public String unlogin(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @RequestMapping(value = "/regist")
    @ResponseBody
    public String regist(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        userModel.uuid = UUIDUtil.getUid();
        userModel.registTime = new java.sql.Date(new Date().getTime());
        int res = userService.regist(userModel);

        RegistModel registModel = new RegistModel();
        registModel.code = 0;
        if (res != 1) {
            registModel.code = 1;
        }
        System.err.println("regist res==>" + res);
        return gson.toJson(registModel);
    }

    @RequestMapping(value = "/getAllFriend")
    @ResponseBody
    public String getAllFriend(@RequestParam(value = "uuid") String uuid) {
        FriendResModel friendResModel = new FriendResModel();
        friendResModel.friends = userService.getAllFriend(uuid);
        friendResModel.code = 0;

        System.err.println("regist res==>" + friendResModel.toString());
        return gson.toJson(friendResModel);
    }

    @RequestMapping(value = "/getAllGroup")
    @ResponseBody
    public String getAllGroup(@RequestParam(value = "uuid") String uuid) {
        GroupResModel resModel = new GroupResModel();
        resModel.groups = userService.getAllGroup(uuid);
        resModel.code = 0;

        System.err.println("regist res==>" + resModel.toString());
        return gson.toJson(resModel);
    }

    @RequestMapping(value = "/createGroup")
    @ResponseBody
    public String createGroup(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "groupName") String groupName) {
        GroupModel groupModel = new GroupModel();
        groupModel.userId = uuid;
        groupModel.groupName = groupName;
        //TODO 创建uuid
        groupModel.groupId = UUIDUtil.getUid();

        List<GroupMember> m = new ArrayList<>();
        GroupMember member = new GroupMember();
        member.userId = uuid;
        m.add(member);

        groupModel.members = groupModel.memToStr(gson, m);

        int res = userService.createGroup(groupModel);

        return "创建群==>" + res + "==" + groupModel.toString();
    }

    @RequestMapping(value = "/delGroup")
    @ResponseBody
    public String delGroup(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "groupId") String groupId) {
        boolean res = false;
        GroupModel groupModel = userService.getGroupInfo(groupId);
        int resDel = userService.delGroup(groupModel);
        L.p("delgroup res=>" + resDel);
        if (groupModel != null) {
            if (groupModel.userId == uuid) {
                List<GroupMember> members = groupModel.getMembers(gson);

                //需要将自己删除
                GroupMember uMem = new GroupMember();
                uMem.userId = uuid;
                members.add(uMem);

                delMembers(groupId, members);

                res = true;
            }
        }

        return "删除成功==>" + res;
    }

    public void delMembers(String groupId, List<GroupMember> members) {
        for (GroupMember mem : members) {
            int res = userService.delMapGroup(mem.userId);
            if (res == 1) {
                RequestMsgModel msgModel = RequestMsgModel.create(Constant.SERVER_UID, mem.userId, 0);
                msgModel.cmd = RequestMsgModel.GROUP_DEL;

                Map<String, MQMapModel> reqMap = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, mem.userId);
                if (reqMap == null) {
                    int check = userService.checkUser(mem.userId);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid  delMembers==>" + mem.userId);
                        break;
                    }
                }

                Set<String> queueSet = new HashSet<>();
                for (MQMapModel value : reqMap.values()) {
                    if (queueSet.contains(value.queueName))
                        break;
                    queueSet.add(value.queueName);

                    rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(MsgType.MSG_CMD_REQ, gson.toJson(msgModel))));
                }
            } else {
                L.e("删除群成员失败==>uid " + mem.userId + "  groupid=>" + groupId);
            }
        }
    }
}
