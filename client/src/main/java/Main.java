import com.example.imlib.netty.IMContext;
import com.example.imlib.netty.IMMsgCallback;
import com.example.imlib.utils.L;
import com.example.imlib.utils.MsgBuild;
import com.example.imlib.utils.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgCmd;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.*;
import utils.Constant;
import utils.UUIDUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * qqq:6f65a65e-bbe6-4bad-b5d2-882b95e091c6/111
 */
public class Main {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new Gson();

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
            public void receive(NimMsg msgModel) {
                L.p("receive==>" + msgModel);

                switch (msgModel.msgType) {
//                    case MsgType.MSG_PACK:
//
//                        break;
//                    case MsgType.MSG_CMD_REQ:
//                        RequestMsgModel reqMsg = (RequestMsgModel) msgModel;
//                        switch (reqMsg.cmd) {
//                            case RequestMsgModel.REQUEST_FRIEND:
//                                //请求好友 默认同意
//                                L.p("是否同意好友请求:" + reqMsg.from + "  Y/N");
////                                String friendAgree = input.next();
////                                if (friendAgree.equals("Y"))
//                                reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_AGREE;
////                                else
////                                    reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_REFUSE;
//                                String tmp = reqMsg.from;
//                                reqMsg.from = reqMsg.to;
//                                reqMsg.to = tmp;
//                                IMContext.instance().sendMsg(reqMsg, true);
//                                break;
//                            case RequestMsgModel.GROUP_ADD:
//                                L.p("是否同意入群请求:" + reqMsg.from + "Y/N");
////                                String groupAgree = input.next();
////                                if (groupAgree.equals("Y"))
//                                reqMsg.cmd = RequestMsgModel.GROUP_ADD_AGREE;
////                                else
////                                    reqMsg.cmd = RequestMsgModel.GROUP_ADD_REFUSE;
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
//                        int del = RequestMsgModel.FRIEND_DEL;
//                        if ("2".equals(dc[1]))
//                            del = RequestMsgModel.FRIEND_DEL_EACH;
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
                                Thread.currentThread().sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "addG":
                        L.p("申请群 群id");
//                        String strg = input.next();
//
//                        RequestMsgModel gModel = RequestMsgModel.create(IMContext.instance().uuid, Constant.SERVER_UID, IMContext.instance().clientToken);
//                        gModel.groupId = strg;
//                        gModel.cmd = RequestMsgModel.GROUP_ADD;
//
//                        IMContext.instance().sendMsg(gModel, true);
                        break;
                    case "exitG":
                        L.p("退出群 群id");
//                        String strge = input.next();
//
//                        RequestMsgModel eModel = RequestMsgModel.create(IMContext.instance().uuid, Constant.SERVER_UID, IMContext.instance().clientToken);
//                        eModel.groupId = strge;
//                        eModel.cmd = RequestMsgModel.GROUP_EXIT;
//
//                        IMContext.instance().sendMsg(eModel, true);
                        break;
                    case "sendG":
                        L.p("发送群消息 uuid/内容");
//                        String strgs = input.next();
//                        String[] gsp = strgs.split("/");
//
//                        GroupMsgModel msgModelG = GroupMsgModel.createG(IMContext.instance().uuid, gsp[0]);
//                        msgModelG.info = gsp[1];
//                        IMContext.instance().sendMsg(msgModelG, true);
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
        OkHttpClient okHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        Request request = new Request.Builder()
                .url(String.format("http://%s/user/login?name=%s&pwd=%s&deviceType=%d", Conf.LOCAL_IP, name, pwd, 1))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("login onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = response.body().string();
                System.err.println("resModel  1111==>" + str);

                BaseModel<UserCheckModel> res = gson.fromJson(str, new TypeToken<BaseModel<UserCheckModel>>() {
                }.getType());
                if (res.success()) {
                    UserCheckModel userModel = res.data;
                    System.err.println("resModel==>" + userModel.toString());
//                    IMContext.instance().setIpList(userModel.serviceList);
                    IMContext.instance().setIpList(Arrays.asList("127.0.0.1:8091"));
                    IMContext.instance().uuid = userModel.uuid;
                    IMContext.instance().clientToken = UUIDUtil.getClientToken();
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
                .url(String.format("http://%s/user/getAllFriend?uuid=%s", Conf.LOCAL_IP, uuid))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = response.body().string();
                System.err.println("resModel  1111==>" + str);
                BaseModel<List<FriendModel>> res = gson.fromJson(str, new TypeToken<BaseModel<List<FriendModel>>>() {
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
                        , Conf.LOCAL_IP, IMContext.instance().uuid, groupName))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resModel  1111==>" + res);

            }
        });
    }

    private void delGroup(long groupId) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/delGroup?uuid=%d&groupId=%d"
                        , Conf.LOCAL_IP, IMContext.instance().uuid, groupId))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resModel  1111==>" + res);

            }
        });
    }

    private void getGroupList(String uuid) {
        Request request = new Request.Builder()
                .url(String.format("http://%s/user/getAllGroup?uuid=%s", Conf.LOCAL_IP, uuid))
                .get()
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
                .url(String.format("http://%s/user/register?name=%s&pwd=%s", Conf.LOCAL_IP, userName, pwd))
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
