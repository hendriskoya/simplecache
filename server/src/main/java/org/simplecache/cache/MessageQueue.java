package org.simplecache.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    
    private final BlockingQueue<String> messages;

    public MessageQueue() {
        messages = new LinkedBlockingQueue<>();
    }

    public void offer(String message) {
        messages.offer(message);
    }

    public String take() throws InterruptedException {
        return messages.take();
    }
}
