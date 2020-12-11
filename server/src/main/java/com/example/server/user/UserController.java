package com.example.server.user;

import com.example.server.ServerList;
import com.example.server.entity.UserModel;
import com.example.server.entity.UserResultModel;
import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import netty.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.*;
import utils.L;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/user")
public class UserController {
    Gson gson = new Gson();

    @Autowired
    UserService userService;

    @Autowired
    UuidManager uuidManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping(value = "/login")
    public String login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
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
        //TODO 暂时调用
        uuidManager.initData();

        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        userModel.uuid = uuidManager.getUuid();
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
    public String getAllFriend(@RequestParam(value = "uuid") long uuid) {
        FriendResModel friendResModel = new FriendResModel();
        friendResModel.friends = userService.getAllFriend(uuid);
        friendResModel.code = 0;

        System.err.println("regist res==>" + friendResModel.toString());
        return gson.toJson(friendResModel);
    }

    @RequestMapping(value = "/getAllGroup")
    @ResponseBody
    public String getAllGroup(@RequestParam(value = "uuid") long uuid) {
        GroupResModel resModel = new GroupResModel();
        resModel.groups = userService.getAllGroup(uuid);
        resModel.code = 0;

        System.err.println("regist res==>" + resModel.toString());
        return gson.toJson(resModel);
    }

    @RequestMapping(value = "/createGroup")
    @ResponseBody
    public String createGroup(@RequestParam(value = "uuid") long uuid, @RequestParam(value = "groupName") String groupName) {
        GroupModel groupModel = new GroupModel();
        groupModel.userId = uuid;
        groupModel.groupName = groupName;
        //TODO 创建uuid
        groupModel.groupId = System.currentTimeMillis() & 0xFFF;

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
    public String delGroup(@RequestParam(value = "uuid") long uuid, @RequestParam(value = "groupId") long groupId) {
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

    public void delMembers(long groupId, List<GroupMember> members) {
        for (GroupMember mem : members) {
            int res = userService.delMapGroup(mem.userId);
            if (res == 1) {
                Vector<SessionModel> users = SessionHolder.sessionMap.get(mem.userId);
                if (users == null || users.isEmpty()) {
                    L.e("delMembers 用户不在线 " + mem.userId);
                    continue;
                }

                for (SessionModel ses : users) {
                    RequestMsgModel msgModel = RequestMsgModel.create(0, mem.userId, ses.deviceTag);
                    msgModel.groupId = groupId;
                    msgModel.cmd = RequestMsgModel.GROUP_DEL;
                    ses.channel.writeAndFlush(msgModel);

                    ReceiptModel receiptMsgModel = new ReceiptModel();
                    receiptMsgModel.channel = new WeakReference<>(ses.channel);
                    receiptMsgModel.msgModel = msgModel;
                    SessionHolder.receiptMsg.put(msgModel.msgId + ses.deviceTag, receiptMsgModel);
                }
            } else {
                L.e("删除群成员失败==>uid " + mem.userId + "  groupid=>" + groupId);
            }
        }
    }
}
