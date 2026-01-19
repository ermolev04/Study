package expression;

public class Negate extends UnaryAbstractAct{
    public Negate(MyExpression exp) {
        super(exp, Type.NEGATE, "-");
    }

    @Override
    protected int calc(int x) {
        return -1 * x;
    }

}
