package org.simplecache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLock {


    public static void main(String[] args) {
        new TestLock().run();
    }

    private void run() {

        MessageService messageService = new MessageService();
        Thread t1 = new Thread(new Worker("t1", messageService));
        Thread t2 = new Thread(new Worker("t2", messageService));
        Thread t3 = new Thread(new Worker("t3", messageService));

        t1.start();
        t2.start();
        t3.start();
    }
}

class Worker implements Runnable {

    private final String name;
    private final MessageService messageService;

    Worker(String name, MessageService messageService) {
        this.name = name;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        while (true) {
            messageService.publish(name);
        }
    }
}

class MessageService {

    private Lock lock = new ReentrantLock(true);
    private Messenger messenger = new Messenger();

    private Messenger getMessenger() {
//        lock.lock();
        return messenger;
    }

    public void publish(String message) {
        Messenger messenger = getMessenger();
        messenger.showMessage(message + " - start");
        try {
            Thread.sleep(5000);
            messenger.showMessage(message + " - finish");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            lock.unlock();
        }
    }

    /*public void sender(String message) {
        Messenger messenger = getMessenger();
        messenger.showMessage(message + " - start");
        try {
            Thread.sleep(5000);
            messenger.showMessage(message + " - finish");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }*/
}

class Messenger {

    public void showMessage(String message) {
        System.out.println(message);
    }
}