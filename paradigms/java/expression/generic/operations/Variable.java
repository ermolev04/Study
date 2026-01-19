package expression.generic.operations;

import expression.generic.MyExpression;
import expression.generic.Type;

public class Variable<T> implements MyExpression<T> {

    private final Type type = Type.VARIABLE;
    String name;
    public Variable(String x) {
        name = x;
    }

    public Type getType() {
        return type;
    }

    public T evaluate(T x) {
        return x;
    }

    @Override
    public T evaluate(T x, T y, T z) {
        switch (name) {
            case "y": return y;
            case "z": return z;
        }
        return x;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Variable<?> exp) {
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
