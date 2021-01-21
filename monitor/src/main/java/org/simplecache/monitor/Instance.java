package org.simplecache.monitor;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instance {

    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);

    private final String name;
    private String ip;
    private String status;
    private final boolean isLocalhost;
    private static Optional<String> hostname;

    static {
        hostname = Optional.ofNullable(System.getenv("HOSTNAME"));
        if (hostname.isEmpty()) {
            LOG.error("Environment variable HOSTNAME is null");
        }
    }

    public Instance(String name, String ip, String status) {
        this.name = name;
        this.ip = ip;
        this.status = status;
        this.isLocalhost = hostname.isEmpty() ? false : hostname.get().equalsIgnoreCase(name);
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocalhost() {
        return isLocalhost;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", localhost='" + isLocalhost + '\'' +
                '}';
    }
}
