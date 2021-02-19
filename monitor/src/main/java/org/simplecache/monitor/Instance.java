package org.simplecache.monitor;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instance {

    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);

    private final String hostname;
    private String ip;
    private String status;
    private final boolean isLocalhost;
    private static final Optional<String> monitorHostname;

    static {
        monitorHostname = Optional.ofNullable(System.getenv("HOSTNAME"));
        if (monitorHostname.isEmpty()) {
            LOG.error("Environment variable HOSTNAME is null");
        }
    }

    public Instance(String hostname, String ip, String status) {
        this.hostname = hostname;
        this.ip = ip;
        this.status = status;
        this.isLocalhost = monitorHostname.isEmpty() ? false : monitorHostname.get().equalsIgnoreCase(hostname);
    }

    public String getHostname() {
        return hostname;
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
                "name='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", localhost='" + isLocalhost + '\'' +
                '}';
    }
}
