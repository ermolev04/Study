package expression;

public class Multiply extends BinaryAbstractAct {

    public Multiply(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.MULTIPLY, "*");
    }


    @Override
    protected int calc(int x, int y) {
        return x * y;
    }


}
