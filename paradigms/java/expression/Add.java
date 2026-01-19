package expression;

public class Add extends BinaryAbstractAct {

    public Add(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.ADD, "+");
    }

    @Override
    protected int calc(int x, int y) {
        return x + y;
    }



}
