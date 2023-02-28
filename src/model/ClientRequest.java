package model;

import enums.Operation;

public class ClientRequest {

    private final Operation operation;
    private final String parameter;

    public ClientRequest(Operation operation, String parameter) {
        this.operation = operation;
        this.parameter = parameter;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getParameter() {
        return parameter;
    }
}
