package com.example.server.netty;

import com.example.server.service.UserService;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.model.BaseMsgModel;
import netty.model.CmdMsgModel;
import netty.model.MsgModel;
import netty.model.MsgType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private Gson gson = new Gson();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    private static NettyServerHandler that;

    @PostConstruct
    public void init() {
        that = this;
    }

    /**
     * 客户端连接会触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("Channel active......");

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsgModel baseMsgModel) throws Exception {
        System.err.println("channelRead0==>" + baseMsgModel.toString());
        switch (baseMsgModel.type) {
            case MsgType.CMD_MSG:
                CmdMsgModel cmdMsg = (CmdMsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case CmdMsgModel.LOGIN:
                        SessionHolder.login(channelHandlerContext.channel(), baseMsgModel);
                        System.err.println("login==>" + baseMsgModel.toString());
                        break;
                    case CmdMsgModel.LOGOUT:
                        SessionHolder.sessionMap.remove(cmdMsg.from);
                        SessionHolder.unlogin(channelHandlerContext.channel());
                        break;
                }
                break;
            case MsgType.MSG_PERSON:
                MsgModel person = (MsgModel) baseMsgModel;
                List<SessionModel> sessionModel = SessionHolder.sessionMap.get(person.to);
                //为null表示未登录
                if (sessionModel == null) {
                    int check = that.userService.checkUser(person.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃
                        System.err.println("不存在的uuid==>" + person.to);
                        break;
                    }
                }

                List<SessionModel> sessionModelSelf = SessionHolder.sessionMap.get(person.from);

                //缓存消息
                String timeLineID = "msg_person:" + Math.min(person.from, person.to) + ":" + Math.max(person.from, person.to);
                that.redisTemplate.opsForList().rightPush(timeLineID, gson.toJson(person));
                System.err.println("timeLineID==>" + that.redisTemplate.opsForList().leftPop(timeLineID));
                //对各个端推送消息
                if (sessionModel != null)
                    for (SessionModel session : sessionModel)
                        if (session != null && session.channel != null)
                            session.channel.writeAndFlush(person);
                //对于自己的非当前客户端 也要推送消息
                if (sessionModelSelf.size() > 1)
                    for (SessionModel session : sessionModelSelf)
                        if (session != null && session.channel != null && session.channel != channelHandlerContext.channel())
                            session.channel.writeAndFlush(person);
                break;
            case MsgType.MSG_GROUP:
                String groupLine = "msg_group:" + baseMsgModel.to;
                that.redisTemplate.opsForList().rightPush(groupLine, gson.toJson(baseMsgModel));
                break;
            case MsgType.RECEIPT_MSG:
                //将回执消息存储
                String receiptLine = "msg_receipt:" + Math.min(baseMsgModel.from, baseMsgModel.to) + ":" + Math.max(baseMsgModel.from, baseMsgModel.to);
                that.redisTemplate.opsForList().rightPush(receiptLine, gson.toJson(baseMsgModel));
                //分发回执消息
                //需要区分群消息 个人消息等
                List<SessionModel> sessionModelSelfReceipt = SessionHolder.sessionMap.get(baseMsgModel.to);
                for (SessionModel session : sessionModelSelfReceipt)
                    if (session != null && session.channel != null && session.channel != channelHandlerContext.channel())
                        session.channel.writeAndFlush(baseMsgModel);
                break;
        }
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}