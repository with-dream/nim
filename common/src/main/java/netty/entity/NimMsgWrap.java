package netty.entity;

import entity.Entity;

public class NimMsgWrap extends Entity {
    public long clientToken;
    public NimMsg msg;

    public static NimMsgWrap buildHeart(NimMsg msg) {
        NimMsgWrap nmw = new NimMsgWrap();
        nmw.msg = msg;
        nmw.clientToken = 0;
        return nmw;
    }

    @Override
    public String toString() {
        return "NimMsgWrap{" +
                "clientToken=" + clientToken +
                ", msg=" + msg +
                '}';
    }
}
