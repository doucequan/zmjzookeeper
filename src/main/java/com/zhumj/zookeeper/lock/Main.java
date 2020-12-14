package com.zhumj.zookeeper.lock;

public class Main {

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {

                Lock lock = new Lock();
                lock.lock("hello");

                System.out.println(Thread.currentThread().getName() + " working......");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock.unLock();

            }).start();
        }
        while (true) {

        }


    }
}
