package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public class CheckedMultiply<T> extends BinaryAbstractAct<T> {

    public CheckedMultiply(MyExpression<T> firstExp, MyExpression<T> secondExp, GenericOperation<T> op) {
        super(firstExp, secondExp, Type.MULTIPLY, "*", op);
    }

    protected T calc(T x, T y) throws MathException {
        return op.Multiply(x, y);
    }

}
