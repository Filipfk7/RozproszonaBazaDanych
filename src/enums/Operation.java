package enums;

import interfaces.RequestHandler;
import model.Node;
import model.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public enum Operation implements RequestHandler {

    SET_VALUE("set-value") {
        @Override
        public void handle(Node node, String parameter) {
            String[] split = parameter.split(":");
            Integer key = Integer.valueOf(split[0]);
            Integer value = Integer.valueOf(split[1]);
            Map<Integer, Integer> record = node.getRecord();
            if (record.containsKey(key)) {
                record.put(key, value);
                Operation.sendResponse(node, OK);
                return;
            }
            Predicate<String> predicate = OK::equals;
            askRelatedNodes(this, node, parameter, predicate);
        }
    },
    GET_VALUE("get-value") {
        @Override
        public void handle(Node node, String parameter) {
            Integer key = Integer.valueOf(parameter);
            Map<Integer, Integer> record = node.getRecord();
            if (record.containsKey(key)) {
                Integer value = record.get(key);
                Operation.sendResponse(node, value.toString());
                return;
            }
            Predicate<String> predicate = Operation::isNumeric;
            askRelatedNodes(this, node, parameter, predicate);
        }
    },
    FIND_KEY("find-key") {
        @Override
        public void handle(Node node, String parameter) {
            Integer key = Integer.valueOf(parameter);
            Map<Integer, Integer> record = node.getRecord();
            if (record.containsKey(key)) {
                Socket socket = node.getSocket();
                String response = socket.getInetAddress().getHostName() + ":" + socket.getPort();
                Operation.sendResponse(node, response);
                return;
            }
            Predicate<String> predicate = s -> {
                if (s == null || s.isEmpty()) {
                    return false;
                }
                String[] split = s.split(":");
                if (split.length != 2) {
                    return false;
                }
                return true;
            };
            askRelatedNodes(this, node, parameter, predicate);

        }
    },
    GET_MAX("get-max") {
        @Override
        public void handle(Node node, String parameter) {
            Set<Integer> maximumValues = new HashSet<>();
            node.getRecord().values().stream()
                    .mapToInt(i -> i)
                    .max()
                    .ifPresent(maximumValues::add);
            searchEdgeValues(this, node, parameter, maximumValues);
            OptionalInt optionalMax = maximumValues.stream()
                    .mapToInt(i -> i)
                    .max();
            if (!optionalMax.isPresent()) {
                Operation.sendResponse(node, ERROR);
                return;
            }
            optionalMax.ifPresent(max -> Operation.sendResponse(node, "" + max));
        }
    },
    GET_MIN("get-min") {
        @Override
        public void handle(Node node, String parameter) {
            Set<Integer> minimumValues = new HashSet<>();
            node.getRecord().values().stream()
                    .mapToInt(i -> i)
                    .min()
                    .ifPresent(minimumValues::add);
            searchEdgeValues(this, node, parameter, minimumValues);
            OptionalInt optionalMin = minimumValues.stream()
                    .mapToInt(i -> i)
                    .min();
            if (!optionalMin.isPresent()) {
                Operation.sendResponse(node, ERROR);
                return;

            }
            optionalMin.ifPresent(min -> Operation.sendResponse(node, "" + min));
        }
    },
    NEW_RECORD("new-record") {
        @Override
        public void handle(Node node, String parameter) {
            String[] split = parameter.split(":");
            Integer key = Integer.valueOf(split[0]);
            Integer value = Integer.valueOf(split[1]);
            Map<Integer, Integer> record = node.getRecord();
            record.clear();
            record.put(key, value);
            Operation.sendResponse(node, OK);
        }
    },
    TERMINATE("terminate") {
        @Override
        public void handle(Node node, String parameter) {
            node.clearServers();
            Operation.sendResponse(node, OK);
        }
    };

    private static final String OK = "OK";
    private static final String ERROR = "ERROR";
    private static final Map<String, Operation> textEnumMap = new HashMap<>();
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    private final String text;


    Operation(String text) {
        this.text = text;
    }

    static {
        for (Operation operation : values()) {
            textEnumMap.put(operation.text, operation);
        }
    }

    public static Operation from(String text) {
        return textEnumMap.get(text);
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(strNum.trim()).matches();
    }

    private static void sendResponse(Node node, String response) {
        try {
            CommunicationHelper.INSTANCE.sendOutput(node.getSocket(), response);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void askRelatedNodes(Operation operation, Node node, String parameter, Predicate<String> predicate) {
        for (Server server : node.getServers()) {
            try(Socket socket = new Socket(server.getHostName(), server.getPort())) {
                String response = askNode(operation, parameter, socket);
                if (predicate.test(response)) {
                    CommunicationHelper.INSTANCE.sendOutput(node.getSocket(), response);
                    return;
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        Operation.sendResponse(node, ERROR);
    }

    private static String askNode(Operation operation, String parameter, Socket socket) throws IOException {
        String request = operation.text + " " + parameter;
        CommunicationHelper.INSTANCE.sendOutput(socket, request);
        return CommunicationHelper.INSTANCE.getInput(socket);
    }

    private static void searchEdgeValues(Operation operation, Node node, String parameter, Set<Integer> edgeValues) {
        Predicate<String> predicate = Operation::isNumeric;
        for (Server server : node.getServers()) {
            try(Socket socket = new Socket(server.getHostName(), server.getPort())) {
                String response = Operation.askNode(operation, parameter, socket);
                if (predicate.test(response)) {
                    edgeValues.add(Integer.valueOf(response));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public String toString() {
        return text;
    }
}

