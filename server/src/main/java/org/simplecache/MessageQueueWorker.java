package org.simplecache;

import org.simplecache.cache.MessageQueue;

public class MessageQueueWorker implements Runnable {

    private final ServersThread serversThread;
    private final MessageQueue messageQueue;

    public MessageQueueWorker(ServersThread serversThread, MessageQueue messageQueue) {
        this.serversThread = serversThread;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = messageQueue.take();

                serversThread.get().forEach(serverHandler -> serverHandler.publish(message));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
