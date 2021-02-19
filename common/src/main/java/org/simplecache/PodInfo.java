package org.simplecache;

import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PodInfo {

    private static final Logger LOG = LoggerFactory.getLogger(PodInfo.class);

    private static final Optional<String> hostname;

    private static final Optional<String> ip;

    private static final LocalDateTime createdAt;

    private PodInfo() {}

    static {
        hostname = Optional.ofNullable(System.getenv("HOSTNAME"));
        if (hostname.isEmpty()) {
            LOG.error("Environment variable HOSTNAME is null");
        }

        ip = Optional.ofNullable(System.getenv("POD_IP"));
        if (ip.isEmpty()) {
            LOG.error("Environment variable POD_IP is null. Check if it is declared in deployment descriptor");
        }

        createdAt = LocalDateTime.now();
    }

    public static String getHostname() {
        return hostname.orElse("unknown");
    }

    public static String getIp() {
        return ip.orElse("unknown");
    }

    public static LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
