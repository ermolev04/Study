package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public abstract class UnaryAbstractAct<T> implements MyExpression<T> {

    protected final MyExpression<T> exp;
    private final Type type;
    private final String sign;
    protected final GenericOperation<T> op;

    protected UnaryAbstractAct(MyExpression<T> exp, Type type, String sign, GenericOperation<T> op) {
        this.exp = exp;
        this.type = type;
        this.sign = sign;
        this.op = op;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryAbstractAct<?> subExp) {
            return type == subExp.type && exp.equals(subExp.exp);
        }
        return false;
    }


    public T evaluate(T x) throws MathException {
        return calc(exp.evaluate(x));
    }


    public T evaluate(T x, T y, T z) throws MathException {
        return calc(exp.evaluate(x, y, z));
    }

    protected T calc(T x) throws MathException {
        return null;
    }

    public String toString() {
        return sign + "(" + exp.toString() + ")";
    }


    public int hashCode(){
        int x = (exp.hashCode() * 53 + type.getValue()) * 29;;
        return x;
    }

}
