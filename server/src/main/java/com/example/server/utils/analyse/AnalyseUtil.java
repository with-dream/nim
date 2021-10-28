package com.example.server.utils.analyse;

import com.example.server.utils.Const;
import netty.entity.MsgCmd;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.NullUtil;

public class AnalyseUtil {
    public static boolean analyse(NimMsg msg) {
        if (!Const.ANALYSE_DEBUG)
            return false;
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PING:
            case MsgType.TYPE_HEART_PONG:
                return false;
            case MsgType.TYPE_PACK:
                return false;
            case MsgType.TYPE_CMD:
                int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
                switch (cmd) {
                    case MsgCmd.LOGIN:
                    case MsgCmd.LOGOUT:
                        return false;
                }
                return true;
        }
        return true;
    }
}
