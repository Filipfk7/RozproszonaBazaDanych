package interfaces;

import model.Node;

public interface RequestHandler {
    void handle(Node node, String parameter);
}
