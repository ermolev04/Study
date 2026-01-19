package expression.exceptions;

import expression.Divide;
import expression.MyExpression;
import expression.Type;

import static java.lang.Integer.MIN_VALUE;

public class CheckedDivide extends BinaryAbstractAct {

    public CheckedDivide(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.DIVIDE, "/");
    }

    @Override
    protected int calc(int x, int y) {
        if(x == MIN_VALUE && y == -1) {
            throw new OverflowException("Overflow in Divide");
        }
        if(y == 0) {
            throw new DivideException("Divide by zero");
        } else {
            return x / y;
        }

    }


}
