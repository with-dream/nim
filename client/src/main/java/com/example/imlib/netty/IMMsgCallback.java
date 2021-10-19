package com.example.imlib.netty;

import netty.entity.NimMsg;

public interface IMMsgCallback {
    void receive(NimMsg msg);
}
