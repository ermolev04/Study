package expression.generic;

import expression.generic.exceptions.MathException;

public interface MyExpression<T> {

    boolean equals(Object obj);

    String toString();

    Type getType();

    int hashCode();

    T evaluate(T x) throws MathException;

    T evaluate(T x, T y, T z) throws MathException;

}
