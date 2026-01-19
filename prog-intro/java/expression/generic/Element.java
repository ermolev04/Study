package expression.generic;

import java.util.ArrayList;
import java.util.List;

public class Element {
    // :NOTE: final
    private final int priority;
    private final Type type;
    private String string;
    private final List<Element> list;
    public Element(Type type, int priority,  String string) {
        this.type = type;
        this.priority = priority;
        this.string = string;
        list = new ArrayList<>();
    }

    public Element(Type type, int priority, List<Element> list) {
        this.type = type;
        this.priority = priority;
        this.list = list;
    }

    public Type getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public List<Element> getList() {
        return list;
    }

    public String getString() {
        return string;
    }
}
