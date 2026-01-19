package expression.generic;

import expression.generic.exceptions.OverflowException;
import expression.generic.exceptions.DivideException;

public interface GenericOperation<T> {

    T Add(T x, T y) throws OverflowException;
    T Subtract(T x, T y) throws OverflowException;
    T Divide(T x, T y) throws DivideException, OverflowException;
    T Multiply(T x, T y) throws OverflowException;
    T Negate(T x) throws OverflowException;

    T ToT(String value);
}