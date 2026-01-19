package expression.exceptions;

import expression.MyExpression;

import static expression.Type.ADD;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class CheckedAdd extends BinaryAbstractAct {

    public CheckedAdd(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, ADD, "+");
    }

    @Override
    protected int calc(int x, int y) {
        if((x > 0 && y > MAX_VALUE - x) || (x < 0 && y < MIN_VALUE - x))  {
            throw new OverflowException("Overflow in Add");
        } else {
            return x + y;
        }

    }



}
