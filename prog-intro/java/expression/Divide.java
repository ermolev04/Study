package expression;

public class Divide extends BinaryAbstractAct {

    public Divide(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.DIVIDE, "/");
    }

    @Override
    protected int calc(int x, int y) {
        return x / y;
    }


}
