package enums;

import java.util.HashMap;
import java.util.Map;

public enum Option {

    TCP_PORT("tcpport"),
    RECORD("record"),
    CONNECT("connect");


    private static final Map<String, Option> textEnumMap = new HashMap<>();

    private final String text;

    Option(String text) {
        this.text = text;
    }

    static {
        for (Option option : values()) {
            textEnumMap.put(option.text, option);
        }
    }

    public static Option from(String text) {
        return textEnumMap.get(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
