package expression.generic;

import expression.generic.exceptions.MathException;
import expression.generic.operations.*;

public class GenericTabulator implements Tabulator {

    @Override
    public Object[][][] tabulate(String mode, String expression, int x1, int x2, int y1, int y2, int z1, int z2) {
        GenericOperation<?> op;
        switch (mode) {
            case "i":
                op = new IntegerOperation();
                break;
            case "d":
                op = new DoubleOperation();
                break;
            case "bi":
                op = new BigIntegerOperation();
                break;
            case "u":
                op = new UncheckedIntegerOperation();
                break;
            case "b":
                op = new ByteOperation();
                break;
            default:
                throw new RuntimeException("Wrong mode value: " + mode);
        }
        return Create(op, expression, x1, x2, y1, y2, z1, z2);
    }

    private <T> Object[][][] Create(GenericOperation<T> op, String expression, int x1, int x2, int y1, int y2, int z1, int z2) {

        ExpressionParser<T> exp = new ExpressionParser<>(op);
        MyExpression<T> expr = exp.parse(expression);
        int n = x2 - x1 + 1, m = y2 - y1 + 1, k = z2 - z1 + 1;
        Object[][][] answer = new Object[n][m][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int l = 0; l < k; l++) {
                    try {
                        answer[i][j][l] = expr.evaluate(op.ToT(Integer.toString(x1 + i)),
                                op.ToT(Integer.toString(y1 + j)), op.ToT(Integer.toString(z1 + l)));
                    } catch (MathException e) {
                        answer[i][j][l] = null;
                    }
                }
            }
        }
        return answer;

    }
}
