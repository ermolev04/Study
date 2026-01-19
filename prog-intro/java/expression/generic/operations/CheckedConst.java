package expression.generic.operations;

import expression.generic.MyExpression;
import expression.generic.Type;

public class CheckedConst<T> implements MyExpression<T> {

    private final T value;
    private final Type type = Type.CONST;
//    private final GenericOperation<T> op;

    public CheckedConst(T value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }


    public T evaluate(T x) {
        return value;
    }


    public T evaluate(T x, T y, T z) {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CheckedConst<?> exp) {
            // :NOTE: unchecked
            return exp.value == value;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int hashCode() {
        return (int) value * 23;
    }
}
