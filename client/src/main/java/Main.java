import com.example.imlib.entity.UserCheckEntity;
import com.example.imlib.netty.IMContext;
import com.example.imlib.netty.IMMsgCallback;
import com.example.imlib.utils.L;
import com.example.imlib.utils.MsgBuild;
import com.example.imlib.utils.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import netty.entity.MsgCmd;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.*;
import utils.Constant;
import utils.RSAUtil;
import utils.UUIDUtil;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

/**
 * qqq:6f65a65e-bbe6-4bad-b5d2-882b95e091c6/111
 */
public class Main {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new Gson();

    public UserCheckEntity userEntity;

    public static void main(String[] args) {
        try {
            m();
        } catch (Exception e) {

        }
    }

    private static void m() {
        Main client = new Main();
        final Scanner input = new Scanner(System.in);
        IMContext.instance().setMsgCallback(new IMMsgCallback() {
            @Override
            public void receive(NimMsg msgEntity) {
                L.p("receive==>" + msgEntity);

                switch (msgEntity.msgType) {
//                    case MsgType.MSG_PACK:
//
//                        break;
//                    case MsgType.MSG_CMD_REQ:
//                        RequestMsgEntity reqMsg = (RequestMsgEntity) msgEntity;
//                        switch (reqMsg.cmd) {
//                            case RequestMsgEntity.REQUEST_FRIEND:
//                                //请求好友 默认同意
//                                L.p("是否同意好友请求:" + reqMsg.from + "  Y/N");
////                                String friendAgree = input.next();
////                                if (friendAgree.equals("Y"))
//                                reqMsg.cmd = RequestMsgEntity.REQUEST_FRIEND_AGREE;
////                                else
////                                    reqMsg.cmd = RequestMsgEntity.REQUEST_FRIEND_REFUSE;
//                                String tmp = reqMsg.from;
//                                reqMsg.from = reqMsg.to;
//                                reqMsg.to = tmp;
//                                IMContext.instance().sendMsg(reqMsg, true);
//                                break;
//                            case RequestMsgEntity.GROUP_ADD:
//                                L.p("是否同意入群请求:" + reqMsg.from + "Y/N");
////                                String groupAgree = input.next();
////                                if (groupAgree.equals("Y"))
//                                reqMsg.cmd = RequestMsgEntity.GROUP_ADD_AGREE;
////                                else
////                                    reqMsg.cmd = RequestMsgEntity.GROUP_ADD_REFUSE;
//                                String tmpGA = reqMsg.from;
//                                reqMsg.from = reqMsg.to;
//                                reqMsg.to = tmpGA;
//                                IMContext.instance().channel.writeAndFlush(reqMsg);
//                                break;
//                        }
//                        break;
                }
            }
        });

        while (true) {
            try {
                String cmd = input.next();
                switch (cmd) {
                    case "q":
                        return;
                    case "register":   //注册
                        L.p("注册 格式:用户名/密码");
                        String nameR = input.next();
                        String[] nR = nameR.split("/");
                        client.register(nR[0], nR[1]);
                        break;
                    case "login":   //格式  用户名/密码
                        L.p("登录 格式:用户名/密码");
                        String name = input.next();
                        String[] n = name.split("/");
                        client.login(n[0], n[1]);
                        break;
                    case "logout":
                        client.logout();
                        break;
                    case "req_friend":   //申请好友
                        L.p("申请好友 对方uuid");
                        String reqUuid = input.next();
//                        client.reqFriend(reqUuid);
                        break;
                    case "del_friend":   //删除好友
//                        L.p("删除好友 对方uuid/指令   1单方删除 2同时删除");
//                        String delCmd = input.next();
//                        String[] dc = delCmd.split("/");
//                        int del = RequestMsgEntity.FRIEND_DEL;
//                        if ("2".equals(dc[1]))
//                            del = RequestMsgEntity.FRIEND_DEL_EACH;
//                        client.delFriend(dc[0], del);
                        break;
                    case "friendList":   //获取所有朋友
                        client.getFriedList(IMContext.instance().uuid);
                        break;
                    case "groupList":   //获取所有朋友
                        client.getGroupList(IMContext.instance().uuid);
                        break;
                    case "self":   //获取所有朋友
                        L.p("self uuid ==>" + IMContext.instance().uuid);
                        break;
                    case "sendP":
                        L.p("发送消息 uuid/内容");
                        String strsp = input.next();
                        String[] p = strsp.split("/");

                        NimMsg msgP = MsgBuild.build(MsgType.TYPE_MSG, p[0]);
                        msgP.msgMap().put(MsgType.KEY_MSG, p[1]);

                        IMContext.instance().sendMsg(msgP);
                        break;
                    case "sendPL":
                        L.p("发送消息 uuid/循环次数/内容");
                        String strspl = input.next();
                        String[] pl = strspl.split("/");

                        int count = Integer.parseInt(pl[1]);
                        for (int i = 0; i < count; i++) {
                            NimMsg msgPL = MsgBuild.build(MsgType.TYPE_MSG, pl[0]);
                            msgPL.msgMap().put(MsgType.KEY_MSG, pl[1]);
                            IMContext.instance().sendMsg(msgPL);
                            try {
                                Thread.currentThread().sleep(3);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "addG":
                        L.p("申请群 群id");
//                        String strg = input.next();
//
//                        RequestMsgEntity gEntity = RequestMsgEntity.create(IMContext.instance().uuid, Constant.SERVER_UID, IMContext.instance().clientToken);
//                        gEntity.groupId = strg;
//                        gEntity.cmd = RequestMsgEntity.GROUP_ADD;
//
//                        IMContext.instance().sendMsg(gEntity, true);
                        break;
                    case "exitG":
                        L.p("退出群 群id");
//                        String strge = input.next();
//
//                        RequestMsgEntity eEntity = RequestMsgEntity.create(IMContext.instance().uuid, Constant.SERVER_UID, IMContext.instance().clientToken);
//                        eEntity.groupId = strge;
//                        eEntity.cmd = RequestMsgEntity.GROUP_EXIT;
//
//                        IMContext.instance().sendMsg(eEntity, true);
                        break;
                    case "sendG":
                        L.p("发送群消息 uuid/内容");
//                        String strgs = input.next();
//                        String[] gsp = strgs.split("/");
//
//                        GroupMsgEntity msgEntityG = GroupMsgEntity.createG(IMContext.instance().uuid, gsp[0]);
//                        msgEntityG.info = gsp[1];
//                        IMContext.instance().sendMsg(msgEntityG, true);
                        break;
                    case "createG":
                        L.p("创建群 群名称");
                        String strgc = input.next();
                        client.createGroup(strgc);
                        break;
                    case "delG":
                        L.p("解散群 groupId");
                        String strgd = input.next();
                        client.delGroup(Long.parseLong(strgd));
                        break;
                }
            } catch (Exception e) {
                L.e("main  e==>" + e.getMessage());
            }
        }
    }

    private void login(String name, String pwd) {
        IMContext.instance().clientToken = UUIDUtil.getClientToken();

        Request request = new Request.Builder()
                .url(String.format("http://%s/user/login?name=%s&pwd=%s&clientToken=%d", Constant.LOCAL_IP, name, pwd, IMContext.instance().clientToken))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("login onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = response.body().string();
                System.err.println("resEntity  1111==>" + str);

                BaseEntity<UserCheckEntity> res = gson.fromJson(str, new TypeToken<BaseEntity<UserCheckEntity>>() {
                }.getType());
                if (res.success()) {
                    userEntity = res.data;
                    System.err.println("resEntity==>" + userEntity.toString());
//                    IMContext.instance().setIpList(userEntity.serviceList);
                    IMContext.instance().setIpList(Arrays.asList(Constant.NETTY_IP));
                    IMContext.instance().uuid = userEntity.uuid;

                    KeyPair pair = RSAUtil.getKeyPair();
                    IMContext.instance().encrypt.privateRSAClientKey = RSAUtil.getPrivateKey(pair);
                    IMContext.instance().encrypt.publicRSAClientKey = RSAUtil.getPublicKey(pair);
                    IMContext.instance().encrypt.publicRSAServerKey = userEntity.rsaPublicKey;

                    PublicKey publicKey = RSAUtil.string2PublicKey(IMContext.instance().encrypt.publicRSAServerKey);
                    byte[] pubClientKeyByte = RSAUtil.publicEncrytype(IMContext.instance().encrypt.publicRSAClientKey.getBytes(), publicKey);
                    String pubClientKey = Base64.getUrlEncoder().encodeToString(pubClientKeyByte);
                    encrypt1(pubClientKey);
                } else {
                    L.p("==>登录失败");
                }

            }
        });
    }

    private void encrypt1(String key) {
        L.p("encrypt1 ct==>" + IMContext.instance().clientToken);
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/encrypt1?uuid=%s&clientToken=%d&key=%s", Constant.LOCAL_IP, userEntity.uuid, IMContext.instance().clientToken, key))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("login onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = response.body().string();
                System.err.println("resEntity  1111==>" + str);

                BaseEntity<String> res = gson.fromJson(str, new TypeToken<BaseEntity<String>>() {
                }.getType());
                if (res.success()) {
                    String key = res.data;

                    PrivateKey privateKey = RSAUtil.string2Privatekey(IMContext.instance().encrypt.privateRSAClientKey);
                    byte[] aesKeyB = RSAUtil.privateDecrypt(Base64.getUrlDecoder().decode(key), privateKey);
                    IMContext.instance().encrypt.aesKey = new String(aesKeyB);
                    L.p("c aesKey==>" + IMContext.instance().encrypt.aesKey);
                    new Thread(() -> IMContext.instance().connect()).start();
                } else {
                    L.p("==>登录失败");
                }

            }
        });
    }

    private void logout() {
        NimMsg msg = MsgBuild.build(MsgType.TYPE_CMD, Constant.SERVER_UID);
        msg.msgMap().put(MsgType.KEY_CMD, MsgCmd.LOGOUT);
        IMContext.instance().logout = true;

        IMContext.instance().sendMsg(msg);
    }

    private void getFriedList(String uuid) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/getAllFriend?uuid=%s", Constant.LOCAL_IP, uuid))
                .get()
                .addHeader("token", userEntity.token)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                L.p("headers==>" + response.headers());
                String str = response.body().string();
                System.err.println("resEntity  1111==>" + str);
                BaseEntity<List<FriendEntity>> res = gson.fromJson(str, new TypeToken<BaseEntity<List<FriendEntity>>>() {
                }.getType());
                if (res.success()) {
                    L.p("getAllFriend==>" + res.data);
                }
            }
        });
    }

    private void createGroup(String groupName) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/createGroup?uuid=%d&groupName=%s"
                        , Constant.LOCAL_IP, IMContext.instance().uuid, groupName))
                .get()
                .addHeader("token", userEntity.token)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resEntity  1111==>" + res);

            }
        });
    }

    private void delGroup(long groupId) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/delGroup?uuid=%d&groupId=%d"
                        , Constant.LOCAL_IP, IMContext.instance().uuid, groupId))
                .get()
                .addHeader("token", userEntity.token)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resEntity  1111==>" + res);

            }
        });
    }

    private void getGroupList(String uuid) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/getAllGroup?uuid=%s", Constant.LOCAL_IP, uuid))
                .get()
                .addHeader("token", userEntity.token)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("getAllGroup  1111==>" + res);

            }
        });
    }

    private void register(String userName, String pwd) {
        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(pwd)) {
            L.e("用户名 密码不能空");
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        Request request = new Request.Builder()
                .url(String.format("http://%s/user/register?name=%s&pwd=%s", Constant.LOCAL_IP, userName, pwd))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                L.p("register==>" + res);

            }
        });
    }
}
