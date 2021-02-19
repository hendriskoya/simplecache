package org.simplecache;

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
                String message = messageQueue.take();

                connectionManager.nodes().forEach(serverHandler -> serverHandler.publish(message));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
