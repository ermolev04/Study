package expression.exceptions;

import expression.Type;

import java.util.ArrayList;
import java.util.List;

public class Element {
    private int priority;
    private final Type type;
    private String string;
    private int varIndex;
    private List<Element> list;
    public Element(Type type, int priority,  String string) {
        this.type = type;
        this.priority = priority;
        this.string = string;
        list = new ArrayList<>();
    }

    public Element(Type type, int priority,  String string, int varIndex) {
        this.type = type;
        this.priority = priority;
        this.string = string;
        list = new ArrayList<>();
        this.varIndex = varIndex;
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

    public int getVarIndex() {
        return varIndex;
    }
}