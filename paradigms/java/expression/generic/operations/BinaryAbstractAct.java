package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public abstract class BinaryAbstractAct<T> implements MyExpression<T> {

    protected final MyExpression<T> firstExp, secondExp;
    private final Type type;
    private final String sign;
    protected final GenericOperation<T> op;

    protected BinaryAbstractAct(MyExpression<T> firstExp, MyExpression<T> secondExp, Type type, String sign, GenericOperation<T> op) {
        this.firstExp = firstExp;
        this.secondExp = secondExp;
        this.type = type;
        this.sign = sign;
        this.op = op;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryAbstractAct<?> exp) {
            // :NOTE: unchecked
            return type == exp.type && firstExp.equals(exp.firstExp) && secondExp.equals(exp.secondExp);
        }
        return false;
    }


    public T evaluate(T x) throws MathException {
        return calc(firstExp.evaluate(x), secondExp.evaluate(x));
    }

    public T evaluate(T x, T y, T z) throws MathException {
        return calc(firstExp.evaluate(x, y, z), secondExp.evaluate(x, y, z));
    }

    abstract protected T calc(T x, T y) throws MathException;

    public String toString() {
        return "(" + firstExp.toString() + " " + sign + " " + secondExp.toString() + ")";
    }


    public int hashCode() {
        return ((firstExp.hashCode() + (secondExp.hashCode() * 41)) * 113 + type.getValue()) * 67;
    }

}
