package com.example.server.user;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UuidManager {
    private AtomicLong uuid;

//    @Resource
//    SysService sysService;
//
//    private static class UuidManagerHoler {
//        private static UuidManager instance = new UuidManager();
//    }
//
//    private UuidManager() {
//    }
//
//    public static UuidManager instance() {
//        return UuidManagerHoler.instance;
//    }

    public void setUuid(long uuid) {
        this.uuid = new AtomicLong(uuid);
    }

    public long getUuid() {
        return this.uuid.incrementAndGet();
    }

    //TODO 需要放在容器启动完成初始化
//    public void initData() {
//        if (this.uuid == null) {
//            long sysUid = sysService.getUuid();
//            this.uuid = new AtomicLong(sysUid);
//        }
//    }
//
//    public void destory() {
//        sysService.saveUuid(uuid.get());
//    }
}
