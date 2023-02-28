package model;

import java.util.Objects;

public class Server {

    private final String hostName;
    private final int port;

    public Server(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return port == server.port && Objects.equals(hostName, server.hostName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, port);
    }
}
