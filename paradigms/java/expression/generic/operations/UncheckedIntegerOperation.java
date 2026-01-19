package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.exceptions.DivideException;
import expression.generic.exceptions.OverflowException;

public class UncheckedIntegerOperation implements GenericOperation<Integer> {

    public Integer Add(Integer x, Integer y) {
        return x + y;
    }

    public Integer Subtract(Integer x, Integer y){
        return x - y;
    }

    public Integer Divide(Integer x, Integer y) throws DivideException {
        if(y == 0) {
            throw new DivideException("Divide by zero");
        } else {
            return x / y;
        }
    }

    public Integer Multiply(Integer x, Integer y) {
        return x * y;
    }

    public Integer Negate(Integer x) throws OverflowException {
        return -1 * x;
    }

    public Integer ToT(String value) {
        return Integer.parseInt(value);
    }
}
