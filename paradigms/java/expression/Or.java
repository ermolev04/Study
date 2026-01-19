package expression;

public class Or extends BinaryAbstractAct {

    public Or(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.OR, "|");
    }

    @Override
    protected int calc(int x, int y) {
        return x | y;
    }



}