package expression;

import java.util.List;

public class Variable implements MyExpression {

    private final Type type = Type.VARIABLE;
    String name;
    int index;
    public Variable(String x) {
        name = x;
    }

    public Variable(int x) {
        index = x;
    }

    public Variable(int x, String name) {
        index = x;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int evaluate(int x) {
        return x;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        switch (name) {
            case "y": return y;
            case "z": return z;
        }
        return x;
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return variables.get(index);
    }


    @Override
    public String toMiniString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Variable) {
            Variable exp = (Variable) obj;
            return name.equals(exp.name);
        }
        return false;
    }



    @Override
    public String toString() {
        return name;
    }

    public int hashCode() {
        switch (name) {
            case "x": return 13;
            case "y": return 101;
            case "z": return 7;
        }
        return 0;
    }
}
