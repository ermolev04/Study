package expression.exceptions;

import expression.MyExpression;
import expression.Type;

import java.util.List;

public class CheckedConst implements MyExpression {

    private final int value;
    private final Type type = Type.CONST;

    public CheckedConst(int value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int evaluate(int x) {
        return value;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return value;
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CheckedConst) {
            CheckedConst exp = (CheckedConst) obj;
            return exp.value == value;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int hashCode() {
        return value * 23;
    }
}
