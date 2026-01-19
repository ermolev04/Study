package expression.exceptions;

import expression.MyExpression;
import expression.Type;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class CheckedSubtract extends BinaryAbstractAct {

    public CheckedSubtract(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.SUBTRACT, "-");
    }

    @Override
    protected int calc(int x, int y) {
        if((x >= 0 && y < -(MAX_VALUE - x)) || (x < 0 && y > -(MIN_VALUE - x))) {
            throw new OverflowException("Overflow in Subtract");
        } else {
            return x - y;
        }
    }


}
