package com.zhumj.zookeeper.lock;

import com.zhumj.zookeeper.ZkUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

// 首先需要创建父节点，
public class Lock implements AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, Watcher, AsyncCallback.StatCallback {
    String sequencePath;

    ZooKeeper zk;

    CountDownLatch countDownLatch = null;

    String ownerName;

    // 表示上锁的次数，可以参考reentrantLock实现，lock和release时，都对这个state
    private int state;


    public void unLock() {
        try {
            zk.delete(sequencePath, -1);
            System.out.println(sequencePath + " unlocked");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }



    public void lock(String key) {
        zk = ZkUtils.getZk("/lock");
        ownerName = Thread.currentThread().getName();
        zk.create("/" + key, ownerName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, null);

        // 重入锁
        if (countDownLatch == null || countDownLatch.getCount() == 0) {
            countDownLatch = new CountDownLatch(1);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // create call back
    @Override
    public void processResult(int i, String path, Object o, String sequencePath) {
        // 创建成功后获取所有同级的节点
        this.sequencePath = sequencePath;
        zk.getChildren("/", false, this, null);

    }

    // get child call back
    @Override
    public void processResult(int i, String parentPath, Object o, List<String> list) {
        Collections.sort(list);
        // 当前索引值最小获取成功
        int myIndex = list.indexOf(sequencePath.substring(1));
        if (myIndex == 0) {
            System.out.println(ownerName + " locked......"  + list.get(0));
//            try {
//                zk.setData("/", ownerName.getBytes(), -1);
//            } catch (KeeperException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            countDownLatch.countDown();
            return;
        }
        // 当前索引值不是最小的，监控前一个节点
        zk.exists("/" + list.get(myIndex - 1), this, this, null);
    }


    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                // 监控的节点删除后，继续判断自身的顺序，尝试获取锁
                zk.getChildren("/", false, this, null);
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }

    // stat call back
    @Override
    public void processResult(int i, String path, Object ctx, Stat stat) {
        if (stat == null) {
            // 如果监控的前一个节点已经不存在了，。
        }
    }
}
