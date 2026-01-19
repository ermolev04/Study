package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.OverflowException;

public class CheckedSubtract<T> extends BinaryAbstractAct<T> {

    public CheckedSubtract(MyExpression<T> firstExp, MyExpression<T> secondExp, GenericOperation<T> op) {
        super(firstExp, secondExp, Type.SUBTRACT, "-", op);
    }

    protected T calc(T x, T y) throws OverflowException {
        return op.Subtract(x, y);
    }


}
