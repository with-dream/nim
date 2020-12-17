
import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZkServer {
    public static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        A a = new A();
        a.aaa = 2;
        System.out.println("a==>" + a.aaa);
        a.aaa = 3;
        System.out.println("a==>" + a.aaa);
        // 1.连接zkServer
//        ZkServer zkServer = new ZkServer();
//        zkServer.getConnect();
//
//        // 2.注册节点信息 服务器ip添加到zk中
//        zkServer.regist(HOST);
//
//        // 3.业务逻辑处理
//        zkServer.build(HOST);
    }

    private String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
    private int sessionTimeout = 3000;
    ZooKeeper zkCli = null;
    // 定义父节点
    private String parentNode = "/servers";

    // 1.连接zkServer
    public void getConnect() throws IOException {
        zkCli = new ZooKeeper(connectString, sessionTimeout, new Watcher() {

            @Override
            public void process(WatchedEvent event) {

            }
        });
    }

    // 2.注册信息
    public void regist(String hostname) throws KeeperException, InterruptedException {
        String node = zkCli.create(parentNode + "/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(node);
    }

    // 3.构造服务器
    public void build(String hostname) throws InterruptedException {
        System.out.println(hostname + ":服务器上线了！");
        Thread.sleep(Long.MAX_VALUE);
    }
}

class A {
    int aaa = 1;
}