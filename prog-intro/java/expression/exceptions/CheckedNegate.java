package expression.exceptions;

import expression.MyExpression;
import expression.Type;

import static java.lang.Integer.MIN_VALUE;

public class CheckedNegate extends UnaryAbstractAct{
    public CheckedNegate(MyExpression exp) {
        super(exp, Type.NEGATE, "-");
    }

    @Override
    protected int calc(int x) {
        if(x == MIN_VALUE) {
            throw new OverflowException("Overflow in Negate");
        } else {
            return -1 * x;
        }

    }

}
