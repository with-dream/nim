package com.example.server.zookeeper;

import com.example.server.rabbitmq.QueueDto;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.quorum.LeaderZooKeeperServer;
import org.springframework.stereotype.Component;
import utils.L;

import java.io.IOException;
import java.util.List;

@Component
public class ZooUtil {
    private static final int sessionTimeout = 3000;
    private ZooKeeper zkCli = null;

    //TODO 是否需要动态增减ip
    public static final String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
    private static final String ZOO_PATH = "/nim";
    private static final String ZOO_LOCK_PATH = "/lock";
    private static final String ZOO_SERVER_PATH = "/server";
    private static final String nodeServerName = ZOO_PATH + ZOO_SERVER_PATH + "/server_";
    private static final String nodeLockName = ZOO_PATH + ZOO_LOCK_PATH + "/lock_";

    public String createNode(String serverName, String data) throws IOException, KeeperException, InterruptedException {
        zkCli = new ZooKeeper(connectString, sessionTimeout, (event) -> {
            try {
                List<String> children = zkCli.getChildren(ZOO_PATH, true);
                for (String child : children) {
                    byte[] d = zkCli.getData(ZOO_PATH + "/" + child, false, null);
                    L.p(child + "==" + new String(d));
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //创建的都是临时节点 如果有重名 说明有问题
        Stat pathNode = zkCli.exists(nodeServerName, true);
        if (pathNode != null) {
            return null;
        }

        Stat path = zkCli.exists(ZOO_PATH, true);
        if (path == null) {
            byte[] nb = new byte[0];
            String n = zkCli.create(ZOO_PATH, nb, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            L.p("创建ZOO_PATH==>" + n);
        }
//        String node = zkCli.create(nodeName, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                CreateMode.EPHEMERAL);
//        L.p("创建server==>" + node);
//        String nodeLock = zkCli.create(nodeName, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                CreateMode.EPHEMERAL);
        return "nodeLock";
    }

    private String createPath(String path, CreateMode createMode) {
        try {
            Stat stat = zkCli.exists(path, true);
            if (stat == null) {
                byte[] nb = new byte[0];
                String n = zkCli.create(ZOO_PATH, nb, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
//                        CreateMode.PERSISTENT);
                L.p("创建ZOO_PATH==>" + n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
