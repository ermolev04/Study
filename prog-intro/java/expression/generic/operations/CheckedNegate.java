package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.MyExpression;
import expression.generic.Type;
import expression.generic.exceptions.MathException;

public class CheckedNegate<T> extends UnaryAbstractAct<T>{
    public CheckedNegate(MyExpression<T> exp, GenericOperation<T> op) {
        super(exp, Type.NEGATE, "-", op);
    }

    protected T calc(T x) throws MathException {
        return op.Negate(x);

    }

}
