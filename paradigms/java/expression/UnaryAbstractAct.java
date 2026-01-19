package expression;

import java.util.List;

public abstract class UnaryAbstractAct implements MyExpression {

    protected final MyExpression exp;
    private final Type type;
    private final String sign;

    protected UnaryAbstractAct(MyExpression exp, Type type, String sign) {
        this.exp = exp;
        this.type = type;
        this.sign = sign;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryAbstractAct) {
            UnaryAbstractAct subExp = (UnaryAbstractAct) obj;
            return type == subExp.type && exp.equals(subExp.exp);
        }
        return false;
    }

    @Override
    public int evaluate(int x) {
        return calc(exp.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return calc(exp.evaluate(x, y, z));
    }

    @Override
    public int evaluate(List<Integer> variables) {
        return calc(exp.evaluate(variables));
    }

    protected int calc(int x) {
        return 0;
    }

    public String toString() {
        return sign + "(" + exp.toString() + ")";
    }


    public int hashCode(){
        int x = (exp.hashCode() * 53 + type.getValue()) * 29;;
        return x;
    }

}
