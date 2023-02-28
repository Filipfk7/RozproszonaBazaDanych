import enums.Operation;
import enums.Option;
import enums.CommunicationHelper;
import model.ClientRequest;
import model.Node;
import model.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseNode {

    public static void main(String[] args) throws IOException {
        Map<Option, Set<String>> options = getOptions(args);
        Map<Integer, Integer> record = getRecord(options);
        Set<Server> servers = getServers(options);
        int tcpPort = getTcpPort(options);
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            System.out.println("model.Server is listening on port " + tcpPort);
            System.out.println(serverSocket);
            while (true) {
                Socket socket = serverSocket.accept();
                Node node = new Node(socket, record, servers);
                String requestBody = CommunicationHelper.INSTANCE.getInput(socket);
                ClientRequest clientRequest = convertToClientRequest(requestBody);
                Operation operation = clientRequest.getOperation();
                String parameter = clientRequest.getParameter();
                operation.handle(node, parameter);
                node.getSocket().close();
                if (operation == Operation.TERMINATE) {
                    serverSocket.close();
                    System.exit(0);
                }
            }
        }
    }

    private static Map<Option, Set<String>> getOptions(String[] args) {
        Map<Option, Set<String>> options = new HashMap<>();
        Option currentOption = null;
        for (String arg : args) {
            if (arg.charAt(0) == '-') {
                Option option = getOption(arg);
                if (options.containsKey(option)) {
                    continue;
                }
                currentOption = option;
                options.put(currentOption, new HashSet<>());
                continue;
            }
            if (currentOption == null) {
                throw new IllegalArgumentException("Argument without option: " + arg);
            }
            options.get(currentOption).add(arg);
        }
        System.out.println(options);
        return options;
    }


    private static Option getOption(String arg) {
        if (arg.length() < 2) {
            throw new IllegalArgumentException("enums.Option without name: " + arg);
        }
        if (arg.charAt(1) == '-') {
            throw new IllegalArgumentException("enums.Option is not '-' type: " + arg);
        }
        String optionValue = arg.substring(1);
        Option option = Option.from(optionValue);
        if (option == null) {
            throw new IllegalArgumentException("Not known option: " + arg);
        }
        return option;
    }

    private static Map<Integer, Integer> getRecord(Map<Option, Set<String>> options) {
        Set<String> records = options.get(Option.RECORD);
        if (records.size() != 1) {
            throw new IllegalStateException("Wrong amount of records");
        }
        return records.stream()
                .findFirst()
                .map(DatabaseNode::toMapRecord)
                .orElseGet(Collections::emptyMap);
    }

    private static Map<Integer, Integer> toMapRecord(String record) {
        Map<Integer, Integer> records = new HashMap<>();
        String[] split = record.split(":");
        Integer key = Integer.valueOf(split[0]);
        Integer value = Integer.valueOf(split[1]);
        records.put(key, value);
        return records;
    }

    private static Set<Server> getServers(Map<Option, Set<String>> options) {
        Option connect = Option.CONNECT;
        if (!options.containsKey(connect)) {
            return new HashSet<>();
        }
        return options.get(connect).stream()
                .map(DatabaseNode::toServer)
                .collect(Collectors.toSet());
    }

    private static Server toServer(String connectString) {
        String[] split = connectString.split(":");
        return new Server(split[0], Integer.parseInt(split[1]));
    }

    private static int getTcpPort(Map<Option, Set<String>> options) {
        Set<String> tcpPorts = options.get(Option.TCP_PORT);
        if (tcpPorts.size() != 1) {
            throw new IllegalStateException("Wrong amount of TCP ports");
        }
        int tcpPort = tcpPorts.stream()
                .findFirst()
                .map(Integer::parseInt)
                .orElse(null);
        System.out.println(tcpPort);
        return tcpPort;
    }
    private static ClientRequest convertToClientRequest(String requestBody) {
        return Optional.ofNullable(requestBody)
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(" "))
                .filter(array -> array.length == 1 || array.length == 2)
                .filter(array -> Operation.from(array[0]) != null)
                .map(DatabaseNode::toClientRequest)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect argument"));
    }

    private static ClientRequest toClientRequest(String[] args) {
        Operation operation = Operation.from(args[0]);
        if (args.length == 1) {
            return new ClientRequest(operation, "");
        }
        return new ClientRequest(operation, args[1]);
    }
}
