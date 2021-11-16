package com.example.server.controller;

import com.alibaba.fastjson.JSON;
import com.example.server.entity.*;
import com.example.server.netty.MsgBuild;
import com.example.server.netty.SendHolder;
import com.example.server.redis.RConst;
import com.example.server.utils.auth.AuthUtil;
import com.example.server.utils.auth.PassToken;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import netty.entity.NimMsgWrap;
import org.apache.commons.lang.StringUtils;
import com.example.server.service.UserService;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.BaseEntity;
import user.CodeInfo;
import utils.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * 1 注册
 * 2 登录
 * 3 登出
 * 一、 好友
 * 1 好友列表
 * 2 添加好友
 * 3 删除好友
 * 4 拉黑
 * 5 取消拉黑
 * 6 备注
 * 7 聊天背景图
 * 8 置顶
 * 二、群成员
 * 1 群列表
 * 2 加群
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
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @Resource
    RedissonClient redisson;

    @Resource
    SendHolder sendHolder;

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

    @PassToken
    @RequestMapping(value = "/login")
    public BaseEntity<UserResEntity> login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd
            , @RequestParam(value = "clientToken") long clientToken) {
        UserEntity userEntity = new UserEntity();
        userEntity.name = name;
        userEntity.pwd = pwd;
        UserResEntity res = userService.login(userEntity);
        boolean loginSuccess = res != null && StringUtils.isNotEmpty(res.uuid);
        if (loginSuccess) {
            //返回rsa公钥
            AESEntity re = new AESEntity();
            KeyPair pair = RSAUtil.getKeyPair();
            re.privateRSAServerKey = RSAUtil.getPrivateKey(pair);
            re.publicRSAServerKey = RSAUtil.getPublicKey(pair);
            re.createTime = System.currentTimeMillis();
            RMap<Long, AESEntity> rsaMap = redisson.getMap(RConst.AES_MAP);
            rsaMap.put(clientToken, re);
            //1 将公钥传给客户端
            res.rsaPublicKey = re.publicRSAServerKey;
            //返回token
            res.token = AuthUtil.createToken(res.uuid);
        }

        L.p("login res==>" + res);
        return loginSuccess ? BaseEntity.succ(res) : BaseEntity.fail();
    }

    @RequestMapping(value = "/encrypt")
    public BaseEntity<UserResEntity> encrypt(@RequestParam(value = "clientToken") long clientToken, @RequestParam(value = "key") String key, HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        RMap<Long, AESEntity> rsaMap = redisson.getMap(RConst.AES_MAP);
        AESEntity re = rsaMap.get(clientToken);

        //2 用服务端的私钥解出客户端的公钥
        PrivateKey privateKey = RSAUtil.string2Privatekey(re.privateRSAServerKey);
        byte[] clientKey = RSAUtil.privateDecrypt(Base64.getDecoder().decode(key), privateKey);
        re.publicRSAClientKey = new String(clientKey);
        //3 将aes的秘钥传给客户端
        re.aesKey = AESUtil.getStrKeyAES();
        rsaMap.put(clientToken, re);
        L.p("s aesKey==>" + rsaMap.get(clientToken).aesKey);
        PublicKey publicKey = RSAUtil.string2PublicKey(re.publicRSAClientKey);
        byte[] aesByte = RSAUtil.publicEncrytype(re.aesKey.getBytes(), publicKey);
        String aes = Base64.getEncoder().encodeToString(aesByte);

        UserResEntity res = new UserResEntity();
        res.serviceList = Constant.SERVER_LIST;
        res.uuid = uuid;
        res.aesPublicKey = aes;
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/logout")
    public String logout(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @RequestMapping(value = "/userInfo")
    @ResponseBody
    public BaseEntity<UserInfoEntity> userInfo(@RequestParam(value = "uuid") String uuid) {
        UserInfoEntity ui = userService.userInfo(uuid);
        return BaseEntity.succ(ui);
    }

    @RequestMapping(value = "/friendList")
    @ResponseBody
    public BaseEntity<List<FriendInfoEntity>> friendList(HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        List<FriendInfoEntity> res = userService.friendList(uuid);
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/addFriendReq")
    @ResponseBody
    public BaseEntity<List<FriendInfoEntity>> addFriendReq(RequestEntity reqEntity, HttpServletRequest request) {
        //只有当前请求用户的uuid可以操作
        String uuid = (String) request.getAttribute("uuid");
        FriendInfoEntity infoEntity = new FriendInfoEntity();
        infoEntity.userId = uuid;
        reqEntity.userId = uuid;
        infoEntity.friendId = reqEntity.friendId;

        if (userService.isFriend(infoEntity))
            return BaseEntity.fail(CodeInfo.FRIEND_FRIEND);
        if (userService.isBlockAny(infoEntity))
            return BaseEntity.fail(CodeInfo.FRIEND_BLOCK);
        reqEntity.status = CodeInfo.FRIEND_REQ_STATE_REQ;
        int ret = userService.addFriendReq(reqEntity);

        NimMsg msg = MsgBuild.build(reqEntity.userId, reqEntity.friendId, MsgType.TYPE_CMD).self();
        msg.msgMap().put(MsgType.KEY_M_REQ_EXTRA, JSON.toJSONString(reqEntity));
        sendHolder.sendMsg(msg);

        return ret == 1 ? BaseEntity.succ() : BaseEntity.failServer();
    }

    @RequestMapping(value = "/friendReqList")
    @ResponseBody
    public BaseEntity<List<RequestEntity>> friendReqList(HttpServletRequest request) {
        String uuid = (String) request.getAttribute("uuid");
        List<RequestEntity> res = userService.friendReqList(uuid);
        return BaseEntity.succ(res);
    }

    @RequestMapping(value = "/addFriendAffirm")
    @ResponseBody
    public BaseEntity<FriendInfoEntity> addFriendAffirm(@RequestParam(value = "userId") String userId, @RequestParam(value = "friendId") String friendId) {
        FriendInfoEntity infoEntity = new FriendInfoEntity();
        infoEntity.userId = userId;
        infoEntity.friendId = friendId;
        infoEntity.isFriend = true;
        infoEntity.insertTime = new Date();

        int ret = userService.addFriendAffirm(infoEntity);
        if (ret == 1) {

            UserInfoEntity uie = userService.userInfo(userId);

            NimMsg msg = MsgBuild.build(userId, friendId, MsgType.TYPE_CMD).self();
            msg.msgMap().put(MsgType.KEY_M_REQ_USER_INFO_EXTRA, JSON.toJSONString(uie));
            sendHolder.sendMsg(msg);

            return BaseEntity.succ(infoEntity);
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/delFriend")
    @ResponseBody
    public BaseEntity delFriend(@RequestParam(value = "userId") String userId, @RequestParam(value = "friendId") String friendId, @RequestParam(value = "delEach") int delEach) {
        int ret = userService.delFriend(userId, friendId, new Date(), delEach);
        if (ret == 1) {
            if (delEach == 1) {
                NimMsg msg = MsgBuild.build(userId, friendId, MsgType.TYPE_CMD).self();
                msg.msgMap().put(MsgType.KEY_M_REQ_DEL_EXTRA, friendId);
                sendHolder.sendMsg(msg);
            }

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/blockFriend")
    @ResponseBody
    public BaseEntity blockFriend(@RequestParam(value = "userId") String userId, @RequestParam(value = "friendId") String friendId) {
        FriendInfoEntity infoEntity = new FriendInfoEntity();
        infoEntity.userId = userId;
        infoEntity.friendId = friendId;
        int ret = userService.blockFriend(infoEntity);
        if (ret == 1) {
            NimMsg msg = MsgBuild.serverMsg(userId, MsgType.TYPE_CMD);
            msg.msgMap().put(MsgType.KEY_M_REQ_BLOG_EXTRA, friendId);
            sendHolder.sendMsg(msg);

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/delBlockFriend")
    @ResponseBody
    public BaseEntity delBlockFriend(@RequestParam(value = "userId") String userId, @RequestParam(value = "friendId") String friendId) {
        FriendInfoEntity infoEntity = new FriendInfoEntity();
        infoEntity.userId = userId;
        infoEntity.friendId = friendId;
        int ret = userService.delBlockFriend(infoEntity);
        if (ret == 1) {
            NimMsg msg = MsgBuild.serverMsg(userId, MsgType.TYPE_CMD);
            msg.msgMap().put(MsgType.KEY_M_REQ_UNBLOG_EXTRA, friendId);
            sendHolder.sendMsg(msg);

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/stickFriend")
    @ResponseBody
    public BaseEntity stickFriend(StickEntity stickEntity) {
        int ret = userService.stickFriend(stickEntity);
        if (ret == 1) {
            NimMsg msg = MsgBuild.serverMsg(stickEntity.userId, MsgType.TYPE_CMD);
            msg.msgMap().put(MsgType.KEY_M_STICK_EXTRA, JSON.toJSON(stickEntity));
            sendHolder.sendMsg(msg);

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/delStickFriend")
    @ResponseBody
    public BaseEntity delStickFriend(StickEntity stickEntity) {
        int ret = userService.delStickFriend(stickEntity);
        if (ret == 1) {
            NimMsg msg = MsgBuild.serverMsg(stickEntity.userId, MsgType.TYPE_CMD);
            msg.msgMap().put(MsgType.KEY_M_UNSTICK_EXTRA, JSON.toJSON(stickEntity));
            sendHolder.sendMsg(msg);

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/addFriendFolder")
    @ResponseBody
    public BaseEntity addFriendFolder(FriendFolderEntity folderEntity) {
        int ret = userService.addFriendFolder(folderEntity);
        if(ret == 1) {

            return BaseEntity.succ();
        }
        return BaseEntity.failServer();
    }

    @RequestMapping(value = "/updateFriendFolder")
    @ResponseBody
    public BaseEntity updateFriendFolder(List<FriendFolderEntity> folderEntityList) {
        int ret = userService.updateFriendFolder(folderEntityList);
        return ret == 1 ? BaseEntity.succ() : BaseEntity.failServer();
    }

    @RequestMapping(value = "/delFriendFolder")
    @ResponseBody
    public BaseEntity delFriendFolder(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "id") int id) {
        int ret = userService.delFriendFolder(uuid, id);
        return ret == 1 ? BaseEntity.succ() : BaseEntity.failServer();
    }

    @RequestMapping(value = "/friendFolderList")
    @ResponseBody
    public BaseEntity<List<FriendFolderEntity>> friendFolderList(@RequestParam(value = "uuid") String uuid) {
        List<FriendFolderEntity> res = userService.friendFolderList(uuid);
        return BaseEntity.succ(res);
    }
}
