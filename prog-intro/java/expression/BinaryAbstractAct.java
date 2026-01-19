package expression;

import java.util.List;

public abstract class BinaryAbstractAct implements MyExpression {

    protected final MyExpression firstExp, secondExp;
    private final Type type;
    private final String sign;

    protected BinaryAbstractAct(MyExpression firstExp, MyExpression secondExp, Type type, String sign) {
        this.firstExp = firstExp;
        this.secondExp = secondExp;
        this.type = type;
        this.sign = sign;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryAbstractAct) {
            BinaryAbstractAct exp = (BinaryAbstractAct) obj;
            return type == exp.type && firstExp.equals(exp.firstExp) && secondExp.equals(exp.secondExp);
        }
        return false;
    }

    @Override
    public int evaluate(int x) {
        return calc(firstExp.evaluate(x), secondExp.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return calc(firstExp.evaluate(x, y, z), secondExp.evaluate(x, y, z));
    }

    public int evaluate(List<Integer> variables) {
        return calc(firstExp.evaluate(variables), secondExp.evaluate(variables));
    }

    protected int calc(int x, int y) {
        return 0;
    }

    public String toString() {
        return "(" +  firstExp.toString() + " " + sign + " " +  secondExp.toString() + ")";
    }


    public int hashCode() {
        int x = ((firstExp.hashCode() + (secondExp.hashCode() * 41)) * 113 + type.getValue()) * 67;
        return x;
    }

}
