package user;

import netty.model.BaseMsgModel;

import java.util.concurrent.atomic.AtomicBoolean;

public class CacheModel {
    public BaseMsgModel baseMsgModel;
    public AtomicBoolean receipt = new AtomicBoolean(false);
}
