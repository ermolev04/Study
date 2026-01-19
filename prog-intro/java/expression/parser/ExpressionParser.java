package expression.parser;

import expression.*;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.toBinaryString;

public class ExpressionParser implements TripleParser {

    @Override
    public MyExpression parse(String expression) {
        Parser parse = new Parser(expression);
        return parseExpr(new ElementSet(parse.read()), 0);
    }

    private MyExpression parseExpr(ElementSet elements, int priority) {
        if(priority == 5) {
            if(elements.hasNext()) {
                Element element = elements.next();
                switch (element.getType()) {
                    case NEGATE:
                        return new Negate(parseExpr(elements, priority));
                    case EXP:
                        return parseExpr(new ElementSet(element.getList()), 0);
                    case VARIABLE:
                        return new Variable(element.getString());
                    case CONST:
                        return new Const(parseInt(element.getString()));
                }
            }
            throw new RuntimeException("Wrong grammar!");
        }
        MyExpression leftExp = parseExpr(elements, priority + 1);
        while (elements.hasNext()) {
            Element element = elements.next();
            if(element.getPriority() == priority) {
                switch (element.getType()) {
                    case OR:
                        leftExp = new Or(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case XOR:
                        leftExp = new Xor(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case AND:
                        leftExp = new And(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case ADD:
                        leftExp = new Add(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case SUBTRACT:
                        leftExp = new Subtract(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case MULTIPLY:
                        leftExp = new Multiply(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case DIVIDE:
                        leftExp = new Divide(leftExp, parseExpr(elements, priority + 1));
                        break;
                    default:
                        throw new RuntimeException("Wrong grammar!");
                }
            } else {
                elements.back();
                break;
            }
        }
        return leftExp;
    }


}


