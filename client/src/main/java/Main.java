import com.example.imlib.Config;
import com.example.imlib.Test;
import com.example.imlib.netty.IMContext;
import com.example.imlib.netty.IMMsgCallback;
import com.example.imlib.user.RegistModel;
import com.example.imlib.utils.L;
import com.example.imlib.utils.StrUtil;
import com.google.gson.Gson;
import netty.model.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.FriendResModel;
import user.GroupResModel;
import user.UserResultModel;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new Gson();

    public static void main(String[] args) throws CloneNotSupportedException {
        m();
    }

    private static void m() {
        Main client = new Main();
        final Scanner input = new Scanner(System.in);
        IMContext.getInstance().setMsgCallback(new IMMsgCallback() {
            @Override
            public void receive(BaseMsgModel msgModel) {
                L.p("receive==>" + msgModel);

                switch (msgModel.type) {
                    case MsgType.REQ_CMD_MSG:
                        RequestMsgModel reqMsg = (RequestMsgModel) msgModel;
                        switch (reqMsg.cmd) {
                            case RequestMsgModel.REQUEST_FRIEND:
                                //请求好友 默认同意
                                L.p("是否同意好友请求:" + reqMsg.from + "  Y/N");
//                                String friendAgree = input.next();
//                                if (friendAgree.equals("Y"))
                                reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_AGREE;
//                                else
//                                    reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_REFUSE;
                                long tmp = reqMsg.from;
                                reqMsg.from = reqMsg.to;
                                reqMsg.to = tmp;
                                IMContext.getInstance().sendMsg(reqMsg);
                                break;
                            case RequestMsgModel.GROUP_ADD:
                                L.p("是否同意入群请求:" + reqMsg.from + "Y/N");
//                                String groupAgree = input.next();
//                                if (groupAgree.equals("Y"))
                                reqMsg.cmd = RequestMsgModel.GROUP_ADD_AGREE;
//                                else
//                                    reqMsg.cmd = RequestMsgModel.GROUP_ADD_REFUSE;
                                long tmpGA = reqMsg.from;
                                reqMsg.from = reqMsg.to;
                                reqMsg.to = tmpGA;
                                IMContext.getInstance().channel.writeAndFlush(reqMsg);
                                break;
                        }
                        break;
                }
            }
        });

        boolean debug = true;
        while (debug) {
            String cmd = input.next();
            switch (cmd) {
                case "q":
                    return;
                case "regist":   //注册
                    L.p("注册 格式:用户名/密码");
                    String nameR = input.next();
                    String[] nR = nameR.split("/");
                    client.regist(nR[0], nR[1]);
                    break;
                case "login":   //格式  用户名/密码
                    L.p("登录 格式:用户名/密码");
                    String name = input.next();
                    String[] n = name.split("/");
                    client.login(n[0], n[1]);
                    break;
                case "req_friend":   //申请好友
                    L.p("申请好友 对方uuid");
                    String reqUuid = input.next();
                    client.reqFriend(Long.parseLong(reqUuid));
                    break;
                case "del_friend":   //删除好友
                    L.p("删除好友 对方uuid/指令   1单方删除 2同时删除");
                    String delCmd = input.next();
                    String[] dc = delCmd.split("/");
                    int del = RequestMsgModel.DEL_FRIEND;
                    if ("2".equals(dc[1]))
                        del = RequestMsgModel.DEL_FRIEND_EACH;
                    client.delFriend(Long.parseLong(dc[0]), del);
                    break;
                case "friendList":   //获取所有朋友
                    client.getFriedList(IMContext.getInstance().uuid);
                    break;
                case "groupList":   //获取所有朋友
                    client.getGroupList(IMContext.getInstance().uuid);
                    break;
                case "self":   //获取所有朋友
                    L.p("self uuid ==>" + IMContext.getInstance().uuid);
                    break;
                case "sendP":
                    L.p("发送消息 uuid/内容");
                    String strsp = input.next();
                    String[] p = strsp.split("/");

                    MsgModel msgModel = MsgModel.createP(IMContext.getInstance().uuid, Long.parseLong(p[0]), IMContext.getInstance().clientTag);
                    msgModel.info = p[1];
                    IMContext.getInstance().sendMsg(msgModel);
                    break;
                case "sendPL":
                    L.p("发送消息 uuid/循环次数/内容");
                    String strspl = input.next();
                    String[] pl = strspl.split("/");

                    int count = Integer.parseInt(pl[1]);
                    for (int i = 0; i < count; i++) {
                        MsgModel msgModelL = MsgModel.createP(IMContext.getInstance().uuid, Long.parseLong(pl[0]), IMContext.getInstance().clientTag);
                        msgModelL.info = i + "==>" + pl[2];
                        IMContext.getInstance().sendMsg(msgModelL);

                        try {
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "addG":
                    L.p("申请群 群id");
                    String strg = input.next();

                    long groupId = Long.parseLong(strg);
                    RequestMsgModel gModel = RequestMsgModel.create(IMContext.getInstance().uuid, 0, IMContext.getInstance().clientTag);
                    gModel.groupId = groupId;
                    gModel.cmd = RequestMsgModel.GROUP_ADD;

                    IMContext.getInstance().sendMsg(gModel);
                    break;
                case "exitG":
                    L.p("退出群 群id");
                    String strge = input.next();

                    long groupIdE = Long.parseLong(strge);
                    RequestMsgModel eModel = RequestMsgModel.create(IMContext.getInstance().uuid, 0, IMContext.getInstance().clientTag);
                    eModel.groupId = groupIdE;
                    eModel.cmd = RequestMsgModel.GROUP_EXIT;

                    IMContext.getInstance().sendMsg(eModel);
                    break;
                case "sendG":
                    L.p("发送群消息 uuid/内容");
                    String strgs = input.next();
                    String[] gsp = strgs.split("/");
                    long groupIds = Long.parseLong(gsp[0]);

                    MsgModel msgModelG = MsgModel.createP(IMContext.getInstance().uuid, groupIds, IMContext.getInstance().clientTag);
                    msgModelG.type = MsgType.MSG_GROUP;
                    msgModelG.info = gsp[1];
                    IMContext.getInstance().sendMsg(msgModelG);
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
        }

    }

    private void login(String name, String pwd) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/login?name=%s&pwd=%s", Conf.LOCAL_IP, name, pwd))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resModel  1111==>" + res);

                UserResultModel resModel = gson.fromJson(res, UserResultModel.class);
                System.err.println("resModel==>" + resModel.toString());
                IMContext.getInstance().setIpList(resModel.imUrl);
                IMContext.getInstance().uuid = resModel.uuid;

                if (resModel.code == 0) {
                    new Thread(() -> IMContext.getInstance().connect()).start();
                }
            }
        });
    }

    private void getFriedList(long uuid) {
        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/getAllFriend?uuid=%d", Conf.LOCAL_IP, uuid))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resModel  1111==>" + res);

                FriendResModel resModel = gson.fromJson(res, FriendResModel.class);
                System.err.println("resModel==>" + resModel.toString());
                if (resModel.code == 0) {

                }
            }
        });
    }

    private void createGroup(String groupName) {
        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/createGroup?uuid=%d&groupName=%s"
                        , Conf.LOCAL_IP, IMContext.getInstance().uuid, groupName))
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
                .url(String.format("http://%s:8080/user/delGroup?uuid=%d&groupId=%d"
                        , Conf.LOCAL_IP, IMContext.getInstance().uuid, groupId))
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

    private void getGroupList(long uuid) {
        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/getAllGroup?uuid=%d", Conf.LOCAL_IP, uuid))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                System.err.println("resModel  1111==>" + res);

                GroupResModel resModel = gson.fromJson(res, GroupResModel.class);
                System.err.println("resModel==>" + resModel.toString());
                if (resModel.code == 0) {

                }
            }
        });
    }

    private void regist(String userName, String pwd) {
        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(pwd)) {
            L.e("用户名 密码不能空");
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/regist?name=%s&pwd=%s", Config.LOCAL_IP, userName, pwd))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                L.p("resModel==>" + res);

                RegistModel resModel = gson.fromJson(res, RegistModel.class);
                if (resModel.code == 0) {
                }
            }
        });
    }

    private void reqFriend(long uuid) {
        RequestMsgModel msgModel = RequestMsgModel.create(IMContext.getInstance().uuid, uuid, IMContext.getInstance().clientTag);
        msgModel.cmd = RequestMsgModel.REQUEST_FRIEND;

        IMContext.getInstance().sendMsg(msgModel);
    }

    /**
     * 删除好友
     */
    private void delFriend(long uuid, int cmd) {
        RequestMsgModel reqModel = RequestMsgModel.create(IMContext.getInstance().uuid, uuid, IMContext.getInstance().clientTag);
        reqModel.cmd = cmd;
        IMContext.getInstance().sendMsg(reqModel);
    }
}
