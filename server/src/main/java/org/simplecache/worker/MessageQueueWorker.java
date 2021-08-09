package org.simplecache.worker;

import org.simplecache.ConnectionManager;
import org.simplecache.cache.CacheEntry;
import org.simplecache.cache.MessageQueue;

public class MessageQueueWorker implements Runnable {

    private final ConnectionManager connectionManager;
    private final MessageQueue messageQueue;

    public MessageQueueWorker(ConnectionManager connectionManager, MessageQueue messageQueue) {
        this.connectionManager = connectionManager;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                CacheEntry message = messageQueue.take();

                connectionManager.nodes().forEach(serverHandler -> serverHandler.publishAsync(message));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
