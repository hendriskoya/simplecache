package org.simplecache.worker;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.simplecache.ConnectionManager;
import org.simplecache.PodInfo;
import org.simplecache.Server;
import org.simplecache.cache.SimpleCache;

public class LoggerWorker implements Runnable {

    private final ConnectionManager connectionManager;

    public LoggerWorker(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("\nCache hashCode: " + SimpleCache.INSTANCE.cacheHashCode() + "\n");

            StringBuilder message = new StringBuilder();
            message.append("\nCurrent active threads: ");
            message.append(Thread.activeCount());

            Set<Thread> threads = Thread.currentThread().getAllStackTraces().keySet();
            String threadGroupName = Thread.currentThread().getThreadGroup().getName();
//                threads.stream().filter(thread -> thread.getThreadGroup().getName().equals(threadGroupName)).forEach(thread -> System.out.println("\t" + thread.getName()));

            String threadsName = threads.stream()
                    .filter(thread -> thread.getThreadGroup().getName().equals(threadGroupName))
                    .map(thread -> thread.getName())
                    .sorted()
                    .collect(Collectors.joining(", "));

            message.append("\n\t");
            message.append(threadsName);

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            message.append("\nInstances: ");
            message.append(connectionManager.nodes().size());
            message.append("\n\t");

            Set<Instance> instances = new TreeSet<>();

            instances.addAll(connectionManager.nodes().stream().map(it -> new Instance(it.getHostname(), it.getIp(), it.getCreatedAt())).collect(Collectors.toList()));
            instances.add(new Instance(PodInfo.getHostname(), PodInfo.getIp(), PodInfo.getCreatedAt(), true));

            instances.forEach(it -> message.append(it.getHostname() + " - " + it.getIp() + " - " + it.getCreatedAt() + (it.isItSelf() ? " - (self)" : "")));

            System.out.println(message.toString());
        }
    }

    class Instance implements Comparable<Instance> {

        private final String hostname;
        private final String ip;
        private final LocalDateTime createdAt;
        private final boolean itSelf;

        Instance(String hostname, String ip, LocalDateTime createdAt) {
            this.hostname = hostname;
            this.ip = ip;
            this.createdAt = createdAt;
            this.itSelf = false;
        }

        Instance(String hostname, String ip, LocalDateTime createdAt, boolean itSelf) {
            this.hostname = hostname;
            this.ip = ip;
            this.createdAt = createdAt;
            this.itSelf = itSelf;
        }

        public String getHostname() {
            return hostname;
        }

        public String getIp() {
            return ip;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public boolean isItSelf() {
            return itSelf;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return createdAt.equals(instance.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(createdAt);
        }

        @Override
        public int compareTo(Instance o) {
            return getCreatedAt().compareTo(o.getCreatedAt());
        }
    }
}
