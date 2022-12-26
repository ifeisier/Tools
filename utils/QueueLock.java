package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueLock {
    private final int size;
    private final List<Lock> lockList;

    public TopicQueueLock() {
        this.size = 32;
        this.lockList = new ArrayList<>(size);
        for (int i = 0; i < this.size; i++) {
            this.lockList.add(new ReentrantLock());
        }
    }

    public void lock(String topicQueueKey) {
        Lock lock = this.lockList.get((topicQueueKey.hashCode() & 0x7fffffff) % this.size);
        lock.lock();
    }

    public void unlock(String topicQueueKey) {
        Lock lock = this.lockList.get((topicQueueKey.hashCode() & 0x7fffffff) % this.size);
        lock.unlock();
    }
}
