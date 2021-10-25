package com.example.server.user;

import com.example.server.entity.*;
import com.example.server.utils.auth.AuthUtil;
import com.example.server.utils.auth.PassToken;
import org.apache.commons.lang.StringUtils;
import com.example.server.service.UserService;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.BaseEntity;
import utils.*;
import com.example.server.entity.UserCheckEntity;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @Resource
    RedissonClient redisson;

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
            //返回token
            res.token = AuthUtil.createToken(res.uuid);
            //返回rsa公钥
            RSAEntity re = new RSAEntity();
            KeyPair pair = RSAUtil.getKeyPair();
            re.privateRSAServerKey = RSAUtil.getPrivateKey(pair);
            re.publicRSAServerKey = RSAUtil.getPublicKey(pair);
            re.createTime = System.currentTimeMillis();
            //TODO key用客户端token uuid会重复
            RMap<String, RSAEntity> rsaMap = redisson.getMap("RSA_map");
            rsaMap.put(res.uuid, re);
            //1 将公钥传给客户端
            res.rsaPublicKey = re.publicRSAServerKey;
        }

        return loginSuccess ? BaseEntity.succ(res) : BaseEntity.fail();
    }

    @PassToken
    @RequestMapping(value = "/encrypt1")
    public BaseEntity<String> encrypt1(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "key") String key) {
        RMap<String, RSAEntity> rsaMap = redisson.getMap("RSA_map");
        RSAEntity re = rsaMap.get(uuid);
        //2 用服务端的私钥解出客户端的公钥
        PrivateKey privateKey = RSAUtil.string2Privatekey(re.publicRSAServerKey);
        byte[] clientKey = RSAUtil.privateDecrypt(key.getBytes(), privateKey);
        re.publicRSAClientKey = new String(clientKey);
        //3 将aes的秘钥传给客户端
        re.aesKey = AESUtil.getStrKeyAES();
        com.example.imlib.utils.L.p("s aesKey==>" + re.aesKey);
        PublicKey publicKey = RSAUtil.string2PublicKey(re.publicRSAClientKey);
        byte[] aesByte = RSAUtil.publicEncrytype(re.aesKey.getBytes(), publicKey);
        String aes = new String(aesByte);
        return BaseEntity.succ(aes);
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
