package com.example.sdk_nim.netty.entity;

import java.io.Serializable;

public class NimMsgWrap implements Serializable {
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
