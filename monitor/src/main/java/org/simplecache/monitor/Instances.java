package org.simplecache.monitor;

import com.sun.xml.internal.rngom.binary.DataExceptPattern;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instances {

    private final Logger LOG = LoggerFactory.getLogger(Instances.class);
    private final String RUNNING_STATUS = "running";

    private final BlockingQueue<Command> queue;
    private final Map<String, Instance> instances;

    public Instances(BlockingQueue<Command> queue) {
        this.queue = queue;
        this.instances = new HashMap<>();
    }

    /*public void add(String name, String ip, String status) {
        LOG.info("Instance {} was added with IP {} and status {}", name, ip, status);
        instances.put(name, new Instance(name, ip, status));
        if (RUNNING_STATUS.equalsIgnoreCase(status)) {
            queue.add("ADD INSTANCE " + name);
        }
    }*/

    public void addOrModify(String name, String ip, String status) {
        LOG.info("Attempt to add or modify instance {} with ip {} and status {}", name, ip, status);
        if (instances.containsKey(name)) {
            LOG.info("Instance {} was modified with IP {} to status {}", name, ip, status);
            Instance instance = instances.get(name);
            LOG.info("Current instance {} status is {}", name, instance.getStatus());
            String previousStatus = instance.getStatus();
            instance.setIp(ip);
            instance.setStatus(status);
            if (!(RUNNING_STATUS.equalsIgnoreCase(status) && status.equalsIgnoreCase(previousStatus))) {
                if (RUNNING_STATUS.equalsIgnoreCase(status)) {
                    queue.add(new Command(Instruction.ADD, instance));
                }
            }
        } else {
            LOG.info("Instance {} was added with IP {} and status {}", name, ip, status);
            instances.put(name, new Instance(name, ip, status));
            if (RUNNING_STATUS.equalsIgnoreCase(status)) {
                queue.add(new Command(Instruction.ADD, instances.get(name)));
            }
        }
    }

    public void delete(String name) {
        LOG.info("Attempt to remove instance {}", name);
        Instance removedInstance = instances.remove(name);
        LOG.info("Instance {} was removed", removedInstance);

        queue.add(new Command(Instruction.REMOVE, removedInstance));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ");
        sb.append(LocalDateTime.now());
        sb.append("\nInstances {");
        instances.forEach((key, value) -> {
            sb.append("\n\t");
            sb.append(value);
        });
        sb.append("\n}");
        return sb.toString();
    }
}