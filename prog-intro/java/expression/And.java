package expression;

public class And extends BinaryAbstractAct {

    public And(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.AND, "&");
    }

    @Override
    protected int calc(int x, int y) {
        return x & y;
    }



}
