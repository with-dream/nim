package com.example.imlib.netty;

import netty.model.BaseMsgModel;

public interface IMMsgCallback {
    void receive(BaseMsgModel msgModel);
}
