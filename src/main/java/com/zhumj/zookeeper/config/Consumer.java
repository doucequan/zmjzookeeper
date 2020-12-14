package com.zhumj.zookeeper.config;

import com.zhumj.zookeeper.ZkConstants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Consumer {

    public static void main(String[] args) throws InterruptedException {
        ZooKeeper zooKeeper = getZk();

        HisMemberConfig config = new HisMemberConfig();
        NodeWatch nodeWatch = new NodeWatch();
        nodeWatch.setZooKeeper(zooKeeper);
        nodeWatch.setConfig(config);

        while (true) {
            // 阻塞至有数据返回
            nodeWatch.waitData();

            System.out.println(config.getData());

            Thread.sleep(2000);

        }

    }

    public static ZooKeeper getZk() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(ZkConstants.zk_servers, 3000, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getState()) {
                        case Unknown:
                            break;
                        case Disconnected:
                            break;
                        case NoSyncConnected:
                            break;
                        case SyncConnected:
                            System.out.println("连接了");
                            countDownLatch.countDown();
                            break;
                        case AuthFailed:
                            break;
                        case ConnectedReadOnly:
                            break;
                        case SaslAuthenticated:
                            break;
                        case Expired:
                            System.out.println("expired");
                            break;
                        case Closed:
                            System.out.println("断开连接了");
                            break;
                        default:
                    }
                }
            });
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

}
