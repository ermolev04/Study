package expression;

public class Subtract extends BinaryAbstractAct {

    public Subtract(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.SUBTRACT, "-");
    }

    @Override
    protected int calc(int x, int y) {
        return x - y;
    }


}
