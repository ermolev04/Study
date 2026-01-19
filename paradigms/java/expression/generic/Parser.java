package expression.generic;

import expression.generic.exceptions.ParseException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public class Parser<T> {
    private int pos;
    private final String expression;
    public Parser(String expression) {
        pos = 0;
        this.expression = expression;
    }


    public List<Element> read(boolean bracket) throws ParseException {
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
                    if (list.size() > 0 && list.get(list.size() - 1).getType().getValue() > Type.NEGATE.getValue()
                            && list.get(list.size() - 1).getType().getValue() < Type.L0.getValue()) {
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
                    list.add(new Element(Type.EXP, 5,  read(true)));
                    break;
                case ')':
                    if(bracket) {
                        return list;
                    } else {
                        throw new ParseException("We find close bracket without open bracket.");
                    }
                case 'x':
                    list.add(new Element(Type.VARIABLE, 5, "x"));
                    break;
                case 'y':
                    list.add(new Element(Type.VARIABLE, 5, "y"));
                    break;
                case 'z':
                    list.add(new Element(Type.VARIABLE, 5, "z"));
                    break;
                case 'l':
                    if(expect("0")) {
                        if(pos < expression.length() && ( isWhitespace(expression.charAt(pos)) || expression.charAt(pos) == '(') ) {
                            list.add(new Element(Type.L0, 5, "l0"));
                        } else {
                            throw new ParseException("After l0 we expect Whitespace or \'(\', on position: " + (pos + 1) );
                        }

                    } else {
                        throw new ParseException("Unknown command: " + curChar);
                    }
                    break;
                case 't':
                    if(expect("0")) {
                        if(pos < expression.length() && ( isWhitespace(expression.charAt(pos)) || expression.charAt(pos) == '(') ) {
                            list.add(new Element(Type.T0, 5, "t0"));
                        } else {
                            throw new ParseException("After t0 we expect Whitespace or \'(\', on position: " + (pos + 1) );
                        }
                    } else {
                        throw new ParseException("Unknown command: " + curChar);
                    }
                    break;
                default:
                    if(isDigit(curChar)) {
                        readNumb(curChar, list);
                    } else {
                        throw new ParseException("Unknown symbol: " + curChar);
                    }

            }
            skipWhitespace();
            if(pos >= expression.length()) {
                if(bracket) {
                    throw new ParseException("We expect close bracket.");
                } else {
                    return list;
                }

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


