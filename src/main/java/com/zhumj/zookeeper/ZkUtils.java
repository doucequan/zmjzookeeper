package com.zhumj.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import sun.swing.StringUIClientPropertyKey;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkUtils {

    public static ZooKeeper getZk(String parentPath) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            String servers = parentPath == null ? ZkConstants.zk_servers : ZkConstants.zk_servers + parentPath;
            ZooKeeper zooKeeper = new ZooKeeper(servers, 3000, new Watcher() {

                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

}
