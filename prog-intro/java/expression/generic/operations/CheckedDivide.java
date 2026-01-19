package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public class CheckedDivide<T> extends BinaryAbstractAct<T> {

    public CheckedDivide(MyExpression<T> firstExp, MyExpression<T> secondExp, GenericOperation<T> op) {
        super(firstExp, secondExp, Type.DIVIDE, "/", op);
    }

    protected T calc(T x, T y) throws MathException {
        return op.Divide(x, y);
    }


}
