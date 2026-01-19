package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.exceptions.DivideException;
import expression.generic.exceptions.OverflowException;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class IntegerOperation implements GenericOperation<Integer> {

    public Integer Add(Integer x, Integer y) throws OverflowException {
        if((x > 0 && y > MAX_VALUE - x) || (x < 0 && y < MIN_VALUE - x))  {
            throw new OverflowException("Overflow in Add");
        } else {
            return x + y;
        }
    }

    public Integer Subtract(Integer x, Integer y) throws OverflowException {
        if((x >= 0 && y < -(MAX_VALUE - x)) || (x < 0 && y > -(MIN_VALUE - x))) {
            throw new OverflowException("Overflow in Subtract");
        } else {
            return x - y;
        }
    }

    public Integer Divide(Integer x, Integer y) throws DivideException, OverflowException {
        if(x == MIN_VALUE && y == -1) {
            throw new OverflowException("Overflow in Divide");
        }
        if(y == 0) {
            throw new DivideException("Divide by zero");
        } else {
            return x / y;
        }
    }

    public Integer Multiply(Integer x, Integer y) throws OverflowException {
        if(x != 0 && y != 0 && (x > 0 && y > 0 && x > Integer.MAX_VALUE / y
                || x < 0 && y < 0 && x < Integer.MAX_VALUE / y
                || x > 0 && y < 0 &&
                y < Integer.MIN_VALUE / x || x < 0 && y > 0 && x < Integer.MIN_VALUE / y)) {
            throw new OverflowException("Overflow in Multiply");
        } else {
            return x * y;
        }
    }

    public Integer Negate(Integer x) throws OverflowException {
        if(x == MIN_VALUE) {
            throw new OverflowException("Overflow in Negate");
        } else {
            return -1 * x;
        }
    }

    public Integer ToT(String value) {
        return Integer.parseInt(value);
    }
}
