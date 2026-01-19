package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.exceptions.DivideException;

public class DoubleOperation implements GenericOperation<Double> {
    @Override
    public Double Add(Double x, Double y) {
        return x + y;
    }

    @Override
    public Double Subtract(Double x, Double y) {
        return x - y;
    }

    @Override
    public Double Divide(Double x, Double y) throws DivideException {
        return x / y;
    }

    @Override
    public Double Multiply(Double x, Double y) {
        return x * y;
    }

    @Override
    public Double Negate(Double x) {
        return -x;
    }

    @Override
    public Double ToT(String value) {
        return Double.parseDouble(value);

    }
}
