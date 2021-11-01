package com.example.sdk_nim.netty;

import com.example.sdk_nim.netty.entity.NimMsg;

public interface IMMsgCallback {
    void receive(NimMsg msg);
}
