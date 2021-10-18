package com.example.server.netty;

import entity.Entity;
import io.netty.channel.Channel;

import java.util.Objects;

public class SessionModel extends Entity {
    public SessionRedisModel redisModel;
    public Channel channel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionModel that = (SessionModel) o;
        return Objects.equals(redisModel, that.redisModel) &&
                Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redisModel, channel);
    }
}
