package expression.exceptions;

import expression.MyExpression;
import expression.Type;

import static java.lang.Integer.*;

public class CheckedT0 extends UnaryAbstractAct {

    public CheckedT0(MyExpression exp) {
        super(exp, Type.T0, "t0");
    }

    @Override
    protected int calc(int x) {
        String s = toBinaryString(x);
        if(x == 0) {
            return 32;
        }
        int ind = s.length() - 1, ans = 0;
        while(ind >= 0 && s.charAt(ind) == '0') {
            ans++;
            ind--;
        }
        return ans;
    }



}