package model;

import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class Node {

    private final Socket socket;
    private final Map<Integer, Integer> record;
    private final Set<Server> servers;

    public Node(Socket socket, Map<Integer, Integer> record, Set<Server> servers) {
        this.socket = socket;
        this.record = record;
        this.servers = servers;
    }

    public Socket getSocket() {
        return socket;
    }

    public Map<Integer, Integer> getRecord() {
        return record;
    }

    public Set<Server> getServers() {
        return servers;
    }

    public void clearServers() {
        servers.clear();
    }

}
