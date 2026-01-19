package expression;

public class Xor extends BinaryAbstractAct {

    public Xor(MyExpression firstExp, MyExpression secondExp) {
        super(firstExp, secondExp, Type.XOR, "^");
    }

    @Override
    protected int calc(int x, int y) {
        return x ^ y;
    }



}