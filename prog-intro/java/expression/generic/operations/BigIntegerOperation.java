package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.exceptions.DivideException;

import java.math.BigInteger;

public class BigIntegerOperation implements GenericOperation<BigInteger> {

    public BigInteger Add(BigInteger x, BigInteger y) {
        return x.add(y);
    }

    public BigInteger Subtract(BigInteger x, BigInteger y) {
        return x.subtract(y);
    }

    public BigInteger Multiply(BigInteger x, BigInteger y) {
        return x.multiply(y);
    }

    public BigInteger Divide(BigInteger x, BigInteger y) throws DivideException {
        if (y.equals(BigInteger.ZERO)) {
            throw new DivideException("Division by zero");
        }
        return x.divide(y);
    }

    public BigInteger Negate(BigInteger x) {
        return x.negate();
    }

    public BigInteger ToT(String value) {
        return new BigInteger(value);
    }

}
