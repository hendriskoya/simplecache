package org.simplecache.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    
    private final BlockingQueue<CacheEntry> messages;

    public MessageQueue() {
        messages = new LinkedBlockingQueue<>();
    }

    public void offer(CacheEntry message) {
        messages.offer(message);
    }

    public CacheEntry take() throws InterruptedException {
        return messages.take();
    }
}
