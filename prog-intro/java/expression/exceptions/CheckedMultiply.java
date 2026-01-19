package expression.exceptions;

import expression.MyExpression;
import expression.Type;

public class CheckedMultiply extends BinaryAbstractAct {

    public CheckedMultiply(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.MULTIPLY, "*");
    }


    @Override
    protected int calc(int x, int y) {
        if(x != 0 && y != 0 && (x > 0 && y > 0 && x > Integer.MAX_VALUE / y
                || x < 0 && y < 0 && x < Integer.MAX_VALUE / y
                || x > 0 && y < 0 &&
                y < Integer.MIN_VALUE / x || x < 0 && y > 0 && x < Integer.MIN_VALUE / y)) {
            throw new OverflowException("Overflow in Multiply");
        } else {
            return x * y;
        }

    }

    private int sign(int x) {
        if (x < 0) {
            return -1;
        }
        if (x == 0) {
            return 0;
        }
        return 1;
    }

}
