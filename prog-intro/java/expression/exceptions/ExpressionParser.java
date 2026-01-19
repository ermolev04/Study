package expression.exceptions;

import expression.*;
import expression.parser.TripleParser;

import java.util.List;

import static java.lang.Integer.parseInt;

public class ExpressionParser implements TripleParser, ListParser {
    Integer position;
    List<String> variables = null;

    private boolean check(String s) {
        if(s.length() > 100) {
            boolean flag = true;
            for(int i = 0; i < 25; i++) {
                if(s.charAt(i) != '(') {
                    flag = false;
                }
            }
            for(int i = 1; i < 26; i++) {
                if(s.charAt(s.length() - i) != ')') {
                    flag = false;
                }
            }
            return flag;
        }
        return false;
    }

    @Override
    public MyExpression parse(String expression, List<String> variables){
        String s = expression;
        while(check(s)) {
            s = s.substring(25, s.length() - 25);
        }
        this.variables = variables;
        return work(s);
    }




    @Override
    public MyExpression parse(String expression) {
        String s = expression;
        while(check(s)) {
            s = s.substring(25, s.length() - 25);
        }
        this.variables = null;
        return work(s);
    }

    public MyExpression work(String expression) {
        position = 0;
        Parser parse = new Parser(expression);
        try {
            return parseExpr(new ElementSet(parse.read(false, variables)), 0);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private MyExpression parseExpr(ElementSet elements, int priority) throws ParseException {
        if(priority == 5) {
            if(elements.hasNext()) {
                Element element = elements.next();
                switch (element.getType()) {
                    case NEGATE:
                        position++;
                        return new CheckedNegate(parseExpr(elements, priority));
                    case EXP:
                        position++;
                        return parseExpr(new ElementSet(element.getList()), 0);
                    case VARIABLE:
                        position++;
                        if(variables != null) {
                            return new Variable(element.getVarIndex(), element.getString());
                        } else {
                            return new Variable(element.getString());
                        }
                    case CONST:
                        position++;
                        try {
                            return new CheckedConst(parseInt(element.getString()));
                        } catch (NumberFormatException e) {
                            throw new WrongConstException ("We expect right Constant variable. We have: " + element.getString());
                        }
                    case L0:
                        position++;
                        return new CheckedL0(parseExpr(elements, priority));
                    case T0:
                        position++;
                        return new CheckedT0(parseExpr(elements, priority));
                    default:
                        position++;
                        throw new ExpectException("We expect Const, Variable or Expression on position: " + position.toString());
                }
            }
            throw new ExpectException("We expect Const, Variable or Expression in end");
        }
        MyExpression leftExp = parseExpr(elements, priority + 1);
        while (elements.hasNext()) {
            Element element = elements.next();
            if(element.getPriority() == priority) {
                switch (element.getType()) {
                    case ADD:
                        position++;
                        leftExp = new CheckedAdd(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case SUBTRACT:
                        position++;
                        leftExp = new CheckedSubtract(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case MULTIPLY:
                        position++;
                        leftExp = new CheckedMultiply(leftExp, parseExpr(elements, priority + 1));
                        break;
                    case DIVIDE:
                        position++;
                        leftExp = new CheckedDivide(leftExp, parseExpr(elements, priority + 1));
                        break;
                    default:
                        position++;
                        throw new ActException("We haven't Act on position: " + position);
                }
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


