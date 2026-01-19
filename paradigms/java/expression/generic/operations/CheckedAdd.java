package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public class CheckedAdd<T> extends BinaryAbstractAct<T> {

    public CheckedAdd(MyExpression<T> firstExp, MyExpression<T> secondExp, GenericOperation<T> op) {
        super(firstExp, secondExp, Type.ADD, "+", op);
    }

    protected T calc(T x, T y) throws MathException {
        return op.Add(x, y);
    }



}
