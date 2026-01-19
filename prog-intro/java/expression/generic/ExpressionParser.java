package expression.generic;

import expression.generic.exceptions.*;
import expression.generic.operations.*;


public class ExpressionParser<T> {

    GenericOperation<T> op;

    public ExpressionParser(GenericOperation<T> op) {
        this.op = op;
    }
    Integer position;

    public MyExpression<T> parse(String expression) {
        position = 0;
        Parser<T> parse = new Parser<>(expression);
        try {
            return parseExpr(new ElementSet(parse.read(false)), 0);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private MyExpression<T> parseExpr(ElementSet elements, int priority) throws ParseException {
        if(priority == 5) {
            if(elements.hasNext()) {
                Element element = elements.next();
                switch (element.getType()) {
                    case NEGATE:
                        position++;
                        return new CheckedNegate<>(parseExpr(elements, priority), op);
                    case EXP:
                        position++;
                        return parseExpr(new ElementSet(element.getList()), 0);
                    case VARIABLE:
                        position++;
                        return new Variable<>(element.getString());
                    case CONST:
                        position++;
                        try {
                            return new CheckedConst<>(op.ToT(element.getString()));
                        } catch (NumberFormatException e) {
                            throw new WrongConstException("We expect right Constant variable. We have: " + element.getString());
                        }
                    default:
                        position++;
                        throw new ExpectException("We expect Const, Variable or Expression on position: " + position.toString());
                }
            }
            throw new ExpectException("We expect Const, Variable or Expression in end");
        }
        MyExpression<T> leftExp = parseExpr(elements, priority + 1);
        while (elements.hasNext()) {
            Element element = elements.next();
            if(element.getPriority() == priority) {
                leftExp = switch (element.getType()) {
                    case ADD -> {
                        position++;
                        yield new CheckedAdd<>(leftExp, parseExpr(elements, priority + 1), op);
                    }
                    case SUBTRACT -> {
                        position++;
                        yield new CheckedSubtract<T>(leftExp, parseExpr(elements, priority + 1), op);
                    }
                    case MULTIPLY -> {
                        position++;
                        yield new CheckedMultiply<>(leftExp, parseExpr(elements, priority + 1), op);
                    }
                    case DIVIDE -> {
                        position++;
                        yield new CheckedDivide<>(leftExp, parseExpr(elements, priority + 1), op);
                    }
                    default -> {
                        position++;
                        throw new ActException("We haven't Act on position: " + position);
                    }
                };
            } else {
                elements.back();
                break;
            }
        }
        if(priority == 0 && !elements.isEnd()) {
            throw new ParseException("You write wrong exception. We can't parse after: " + position + " element out of " + elements.getSize());
        } else {
            return leftExp;
        }

    }


}
