package expression;

public interface MyExpression extends Expression, TripleExpression, ListExpression {

    boolean equals(Object obj);

    String toString();

    Type getType();

    int hashCode();



}
