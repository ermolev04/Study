package expression.generic.operations;

import expression.generic.GenericOperation;
import expression.generic.exceptions.DivideException;
import expression.generic.exceptions.OverflowException;

public class ByteOperation implements GenericOperation<Byte> {

    public Byte Add(Byte x, Byte y) {
        return (byte) (x + y);
    }

    public Byte Subtract(Byte x, Byte y) {
        return (byte) (x - y);
    }

    public Byte Divide(Byte x, Byte y) throws DivideException {
        if (y == 0) {
            throw new DivideException("Divide by zero");
        } else {
            return (byte) (x / y);
        }
    }

    public Byte Multiply(Byte x, Byte y) {
        return (byte) (x * y);
    }

    public Byte Negate(Byte x) throws OverflowException {
        return (byte) (-1 * x);
    }

    public Byte ToT(String value) {
        return (byte) Integer.parseInt(value);
    }
}
