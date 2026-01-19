package expression.parser;

import expression.TripleExpression;
import expression.Type;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public class Parser {
    private int pos;
    private final String expression;

    public Parser(String expression) {
        pos = 0;
        this.expression = expression;
    }


    public List<Element> read() {
        List<Element> list = new ArrayList<>();
        while(pos < expression.length()) {
            skipWhitespace();
            char curChar = next();
            switch (curChar) {
                case '|':
                    list.add(new Element(Type.OR, 0,  "|"));
                    break;
                case '^':
                    list.add(new Element(Type.XOR, 1,  "^"));
                    break;
                case '&':
                    list.add(new Element(Type.AND, 2,  "&"));
                    break;
                case '+':
                    list.add(new Element(Type.ADD, 3,  "+"));
                    break;
                case '-':
                    if (list.size() > 0 && list.get(list.size() - 1).getType().getValue() > Type.NEGATE.getValue()) {
                        list.add(new Element(Type.SUBTRACT, 3,  "-"));
                    } else {
                        if(pos < expression.length() && isDigit(expression.charAt(pos))){
                            readNumb(curChar, list);
                        } else {
                            list.add(new Element(Type.NEGATE, 5, "-"));
                        }
                    }
                    break;
                case '*':
                    list.add(new Element(Type.MULTIPLY, 4,  "*"));
                    break;
                case '/':
                    list.add(new Element(Type.DIVIDE, 4, "/"));
                    break;
                case '(':
                    list.add(new Element(Type.EXP, 5,  read()));
                    break;
                case ')':
                    return list;
                case 'x':
                    list.add(new Element(Type.VARIABLE, 5, "x"));
                    break;
                case 'y':
                    list.add(new Element(Type.VARIABLE, 5, "y"));
                    break;
                case 'z':
                    list.add(new Element(Type.VARIABLE, 5, "z"));
                    break;
                default:
                    if(isDigit(curChar)) {
                        readNumb(curChar, list);
                    }
            }
            skipWhitespace();
            if(pos >= expression.length()) {
                return list;
            }
        }
        return list;
    }


    private void readNumb(char curChar, List<Element> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(curChar);
        while(pos < expression.length()) {
            char newChar = next();
            if(isDigit(newChar)) {
                sb.append(newChar);
            } else {
                pos--;
                break;
            }
        }
        list.add(new Element(Type.CONST, 5, sb.toString()));
    }

    private boolean expect(String s) {
        int shift = 0;
        int startPos = pos;
        while(shift < s.length() && pos < expression.length() && s.charAt(shift) == next()) {
            shift++;
        }
        if(shift >= s.length()) {
            return true;
        } else {
            pos = startPos;
            return false;
        }
    }

    private void skipWhitespace() {
        while(pos < expression.length() && isWhitespace(expression.charAt(pos)) ) {
            pos++;
        }
    }

    private char next() {
        return expression.charAt(pos++);
    }



}


