package com.zhumj.zookeeper.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NodeWatch implements Watcher, AsyncCallback.DataCallback, AsyncCallback.StatCallback {

    private ZooKeeper zooKeeper;

    private volatile CountDownLatch waitDataLatch;

    private volatile HisMemberConfig config;

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public HisMemberConfig getConfig() {
        return config;
    }

    public void setConfig(HisMemberConfig config) {
        this.config = config;
    }

    /**
     * 等待获取数据，该方法执行结束即表明getData返回了
     */
    public void waitData() {
        if (waitDataLatch == null || waitDataLatch.getCount() == 0) {
            waitDataLatch = new CountDownLatch(1);
        }
        zooKeeper.exists("/1", null, this, null);
        try {
            boolean hasData = waitDataLatch.await(6, TimeUnit.SECONDS);
            if (!hasData) {
                throw new RuntimeException("超时未获取到数据");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理获取数据的回调
     * @param i
     * @param s
     * @param o
     * @param bytes
     * @param stat
     */
    public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
        if (bytes != null) {
            config.setData(new String(bytes));
            waitDataLatch.countDown();
        }
    }

    /**
     * 处理获取stat的回调
     * @param i
     * @param s
     * @param o
     * @param stat
     */
    public void processResult(int i, String s, Object o, Stat stat) {
        if (stat != null) {
            // 注册get事件
            zooKeeper.getData("/1", this, this, null);
        }
    }

    /**
     * 处理node事件的回调
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                break;
            case NodeDataChanged:
                // 注册get事件
                zooKeeper.getData("/1", this, this, null);
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }
}
