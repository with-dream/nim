//package com.example.server.netty;
//
//import com.example.server.ApplicationRunnerImpl;
//import com.example.server.service.UserService;
//import com.example.server.utils.Const;
//import com.google.gson.Gson;
//import io.netty.channel.Channel;
//import netty.MQWrapper;
//import netty.entity.MsgType;
//import netty.entity.NimMsg;
//import org.springframework.amqp.core.AmqpTemplate;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import com.example.server.entity.GroupMemberModel;
//import utils.Constant;
//import utils.Errcode;
//import utils.L;
//import utils.StrUtil;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import java.util.*;
//
///**
// * 整个服务器的消息发送
// */
//@Component
//public class SessionServerHolder {
//    public static final String MSGID_MAP = "msg_map:";
//    public static final String MSGID_OFFLINE = "offline_msgid:";
//
//    private static SessionServerHolder that;
//
//    @PostConstruct
//    public void init() {
//        that = this;
//    }
//
//    @Resource
//    public AmqpTemplate rabbit;
//
//    @Resource
//    public RedisTemplate<String, Object> redisTemplate;
//
//    @Resource
//    public UserService userService;
//
//    private Gson gson = new Gson();
//
//    /**
//     * 1 如果所在客户端平台已登录 则强制退出
//     * 2 将uuid和channel做映射 用于消息推送
//     * 3 将uuid和mq做映射 用于将消息路由到本服务器进行推送
//     */
//    public void login(Channel channel, NimMsg msg) {
//        Object map = that.redisTemplate.opsForHash().get(Const.mqTag(msg.deviceType), msg.from);
//        if (map != null) {
//            //踢掉已经登录的用户 如果在本服务器 直接强制退出
//            SessionRedisModel oldMQ = (SessionRedisModel) map;
//            if (oldMQ.queueName.equals(ApplicationRunnerImpl.MQ_NAME)) {
//
//            } else {
//                //TODO 通过mq发送命令强制退出
//            }
//        }
//
//        SessionHolder.login(channel, msg);
//        //将uuid和mq做映射 用于其他服务器转发消息
//        SessionRedisModel sessionModel = new SessionRedisModel();
//        sessionModel.clientToken = msg.fromToken;
//        sessionModel.deviceType = msg.deviceType;
//        sessionModel.uuid = msg.from;
//        sessionModel.queueName = ApplicationRunnerImpl.MQ_NAME;
//        //每个客户端平台对应一个登录实例 记录客户端的登录信息以及所在的mq名称 用于消息转发
//        that.redisTemplate.opsForHash().put(Const.mqTag(msg.deviceType), msg.from, sessionModel);
//        //测试用
////        SessionRedisModel tmp = (SessionRedisModel) that.redisTemplate.opsForHash().get(Const.mqTag(msg.deviceType), msg.from);
////        L.p("login redis==>" + tmp);
////        L.p("getSessionRedis redis==>" + getSessionRedis(Arrays.asList("qqq", "www")));
//        //用于退出
//        that.redisTemplate.opsForSet().add(ApplicationRunnerImpl.MQ_NAME, msg.from + ":" + msg.deviceType);
//    }
//
//    public void logout(Channel channel) {
//        SessionModel sessionModel = SessionHolder.logout(channel);
//        if (sessionModel != null) {
//            that.redisTemplate.opsForHash().delete(Const.mqTag(sessionModel.deviceType), sessionModel.uuid);
//            that.redisTemplate.opsForSet().remove(ApplicationRunnerImpl.MQ_NAME, sessionModel.uuid + ":" + sessionModel.deviceType);
//        } else {
//            throw new RuntimeException("关闭channel异常");
//        }
//        channel.close();
//    }
//
//    /**
//     * 缓存消息
//     */
//    public boolean saveMsg(String timeLine, NimMsg msg) {
//        Long index = that.redisTemplate.opsForList().rightPush(timeLine, msg);
//        if (index == null) {
//            L.e("saveMsg==>存储消息失败");
//            return false;
//        }
//        that.redisTemplate.opsForList().rightPush(MSGID_MAP + timeLine, msg.msgId);
//
//        return true;
//    }
//
//    /**
//     * 根据uuid 获取所有在线客户端信息
//     * <p>
//     * 已经丢弃了本机的queueName 本服务器不需要转发
//     */
//    public List<SessionRedisModel> getSessionRedis(List<String> uuidList) {
//        List<SessionRedisModel> sessionList = new ArrayList<>();
//        for (String tag : Const.mqTagList()) {
//            for (String uuid : uuidList) {
//                Object model = that.redisTemplate.opsForHash().get(tag, uuid);
//                //queueName相同 表示在本机 丢弃
//                if (model != null && !((SessionRedisModel) model).queueName.equals(ApplicationRunnerImpl.MQ_NAME))
//                    sessionList.add((SessionRedisModel) model);
//            }
//        }
//        return sessionList;
//    }
//
//    /**
//     * 发送群消息
//     */
//    public int sendGroupMsq(NimMsg msg) {
//        String timeLineId = "msg_g:" + msg.to;
//
//        //获取群信息
//        List<GroupMemberModel> memList = that.userService.getGroupMembers(msg.getGroupId());
//        if (memList == null) {
//            boolean check = that.userService.checkGroup(msg.getGroupId());
//            if (!check) {
//                //TODO 如果groupId不存在 则丢弃 否则缓存
//                System.err.println("不存在的groupId  MSG_GROUP==>" + msg.getGroupId());
//                return Errcode.NO_GROUP;
//            }
//        }
//        //获取群成员
//        List<String> uuidList = new ArrayList<>();
//        for (GroupMemberModel m : memList)
//            uuidList.add(m.uuid);
//        //获取所有的在线成员 并将相同queueName的成员
//        List<SessionRedisModel> memSessionList = getSessionRedis(uuidList);
//        if (!memSessionList.isEmpty()) {
//            Map<String, NimMsg> gMap = new HashMap<>();
//            for (SessionRedisModel srm : memSessionList) {
//                //先推送连接本服务器的客户端
//                if (srm.queueName.equals(ApplicationRunnerImpl.MQ_NAME)
//                        && srm.clientToken != msg.fromToken) {
//                    NimMsg tmpMsg = msg.copy();
//                    tmpMsg.to = srm.uuid;
//                    SessionHolder.sendMsg(tmpMsg, false);
//                } else {
//                    //将相同服务器的所有目标uuid打包 统一发送
//                    NimMsg gmm = gMap.get(srm.queueName);
//                    Set<String> toSet = null;
//                    if (gmm == null) {
//                        gmm = msg.copy();
//                        gMap.put(srm.queueName, gmm);
//                    }
//                    toSet = gmm.msgGet(MsgType.KEY_UNIFY_SERVICE_GROUP_UUID_LIST);
//                    if (toSet == null) {
//                        toSet = new HashSet<>();
//                        gmm.msgPut(MsgType.KEY_UNIFY_SERVICE_GROUP_UUID_LIST, toSet);
//                    }
//                    toSet.add(srm.uuid);
//                }
//            }
//            //发送到其他服务器
//            for (Map.Entry<String, NimMsg> entry : gMap.entrySet())
//                that.rabbit.convertAndSend(entry.getKey(), entry.getValue());
//        }
//
//        saveMsg(timeLineId, msg);
//
//        return Errcode.SUCC;
//    }
//
//    /**
//     * 发送单条消息
//     */
//    public int sendMsg(NimMsg msg, Channel channel, String timeLintTag, boolean self) {
//        //查找接受用户的uuid 获取信息
//        List<String> uuidList = new ArrayList<>();
//        uuidList.add(msg.to);
//        if (self)
//            uuidList.add(msg.from);
//        List<SessionRedisModel> sessionList = getSessionRedis(uuidList);
//
//        String timeLineId = StrUtil.getTimeLine(msg.from, msg.to, timeLintTag);
//
//        if (Const.DEBUG) {
//            L.p("sendMsq==>" + sessionList);
//        }
//
//        //检查其他服务器是否有此用户
//        boolean toEmpty = true;
//        for (SessionRedisModel srm : sessionList)
//            if (srm.uuid.equals(msg.to)) {
//                toEmpty = false;
//                break;
//            }
//        //检查本服务器是否有此用户
//        if (toEmpty)
//            toEmpty = SessionHolder.checkSession(msg.to);
//        if (toEmpty) {
//            int check = that.userService.checkUser(msg.to);
//            if (check == 0) {
//                //TODO 如果uuid不存在 则丢弃 否则缓存
//                System.err.println("不存在的uuid  MSG_CMD_REQ==>" + msg.to);
//                return Errcode.NOBODY;
//            }
//            msg.status = BaseMsgModel.OFFLINE;
//            saveOfflineMsgId(channel, msg, timeLineId);
//            return Errcode.OFFLINE;
//        }
//
//        SessionHolder.sendMsg(msg, self);
//
//        //转发到其他服务器
//        Set<String> queueTmp = new HashSet<>();
//        for (SessionRedisModel session : sessionList) {
//            //如果多个客户端在同一个服务器 只需要发送一份
//            if (queueTmp.contains(session.queueName))
//                continue;
//            queueTmp.add(session.queueName);
//
//            that.rabbit.convertAndSend(session.queueName, gson.toJson(new MQWrapper(msg.type, gson.toJson(msg))));
//        }
//        //缓存消息
//        saveMsg(timeLineId, msg);
//
//        return Errcode.SUCC;
//    }
//
//    /**
//     * 保存离线消息
//     */
//    public void saveOfflineMsgId(Channel channel, NimMsg msg, String timeLine) {
//        that.redisTemplate.opsForHash().put(MSGID_OFFLINE + msg.to, msg.msgId, timeLine);
//
//        //TODO 离线回复已发送 可以和SEND_SUC一起回复
//        ReceiptMsgModel receiptModel = ReceiptMsgModel.create(msgModel.to, msgModel.from, msgModel.msgId, Constant.SERVER_TOKEN);
//        receiptModel.cmd = MsgCmd.CLIENT_RECEIVED;
//        receiptModel.sendMsgType = msgModel.type;
//        receiptModel.toToken = msgModel.fromToken;
//        channel.writeAndFlush(receiptModel);
//    }
//
//    //TODO 用于离线后再次上线 拉取缓存消息 不完善
//    public void sendOfflineMsg(String uuid) {
//        Map<Object, Object> offMsg = that.redisTemplate.opsForHash().entries(MSGID_OFFLINE + uuid);
//        if (offMsg.isEmpty())
//            return;
//        PackMsgModel msgModel = PackMsgModel.create(Constant.SERVER_UID, uuid, Constant.SERVER_TOKEN);
//
//        Map<String, List<Long>> tmpMap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : offMsg.entrySet()) {
//            String timeline = (String) entry.getValue();
//            List<Long> list = tmpMap.computeIfAbsent(timeline, k -> new ArrayList<>());
//            list.add((Long) entry.getKey());
//        }
//
//        for (Map.Entry<String, List<Long>> entry : tmpMap.entrySet()) {
//            String timeLineMap = MSGID_MAP + entry.getKey();
//            String timeLineMsg = entry.getKey();
//            List<Long> list = tmpMap.get(entry.getKey());
//            for (long msgId : list) {
//                Long index = that.redisTemplate.opsForList().indexOf(timeLineMap, msgId);
//                if (index == null) continue;
//
//                BaseMsgModel msg = (BaseMsgModel) that.redisTemplate.opsForList().index(timeLineMsg, index);
//                if (msg != null) {
////                    msgModel.addMsg(gson.toJson(msg));
//                } else {
//                    L.e("sendOfflineMsg获取msg为null==>" + index);
//                }
//
//            }
//        }
//
//        L.e("sendOfflineMsg==>" + msgModel.toString());
//
////        sendReceiptMsq(msgModel, )
//    }
//
//}
