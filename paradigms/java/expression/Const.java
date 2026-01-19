package expression;

import java.util.List;

public class Const implements MyExpression{

    private final int value;
    private final Type type = Type.CONST;

    public Const(int value) {
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

//    @Override
//    public String toMiniString() {
//        return String.valueOf(value);
//    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Const) {
            Const exp = (Const) obj;
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
