package org.simplecache;

import java.util.Optional;

public final class Environment {

    private final String SERVER_PORT = "SERVER_PORT";
    private final String DEFAULT_SERVER_PORT = "6100";

    private final String CLIENT_PORT = "CLIENT_PORT";
    private final String DEFAULT_CLIENT_PORT = "5959";

    private final int serverPort;
    private final int clientPort;

    public Environment() {
        serverPort = Integer.valueOf(Optional.ofNullable(System.getenv(SERVER_PORT)).orElse(DEFAULT_SERVER_PORT));
        clientPort = Integer.valueOf(Optional.ofNullable(System.getenv(CLIENT_PORT)).orElse(DEFAULT_CLIENT_PORT));
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getClientPort() {
        return clientPort;
    }
}
