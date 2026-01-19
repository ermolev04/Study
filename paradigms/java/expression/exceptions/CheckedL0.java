package expression.exceptions;

import expression.MyExpression;
import expression.Type;

import static java.lang.Integer.*;

public class CheckedL0 extends UnaryAbstractAct {

    public CheckedL0(MyExpression exp) {
        super(exp, Type.L0, "l0");
    }

    @Override
    protected int calc(int x) {
        if(x == 0) {
            return 32;
        } else {
            String s = toBinaryString(x);
            return 32 - s.length();
        }
    }



}