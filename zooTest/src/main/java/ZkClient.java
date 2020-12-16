
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

// 客户端
public class ZkClient {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        // 1.获取连接
        ZkClient zkClient = new ZkClient();
        zkClient.getConnect();

        // 2.监听服务的节点信息
        zkClient.getServers();

        // 3.业务逻辑（一直监听）
        zkClient.getWatch();
    }

    // 3.业务逻辑
    public void getWatch() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    // 2.监听服务的节点信息
    public void getServers() throws KeeperException, InterruptedException {
        List<String> children = zkCli.getChildren("/servers", true);
        ArrayList<String> serverList = new ArrayList<String>();

        // 获取每个节点的数据
        for (String c : children) {
            byte[] data = zkCli.getData("/servers/" + c, true, null);
            serverList.add(new String(data));
        }

        // 打印服务器列表
        System.out.println("getServers==>" + serverList);
    }

    private String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
    private int sessionTimeout = 3000;
    ZooKeeper zkCli = null;

    // 1.连接集群
    public void getConnect() throws IOException {
        zkCli = new ZooKeeper(connectString, sessionTimeout, new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                List<String> children = null;
                try {
                    // 监听父节点
                    children = zkCli.getChildren("/servers", true);
                    Stat stat = zkCli.exists("", false);

                    // 创建集合存储服务器列表
                    ArrayList<String> serverList = new ArrayList<String>();

                    // 获取每个节点的数据
                    for (String c : children) {
                        byte[] data = zkCli.getData("/servers/" + c, true, null);
                        serverList.add(new String(data));
                    }

                    // 打印服务器列表
                    System.out.println("process==>" + serverList);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}