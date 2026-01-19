package expression.generic;

public enum Type {
    OR(1000),
    XOR(2000),
    AND(3000),
    ADD(4000), SUBTRACT(5000),
    MULTIPLY(6000), DIVIDE(7000),
    NEGATE(8000),
    CONST(9000), VARIABLE(10000),
    EXP(11000),
    L0(12000), T0(13000);

    private final int value;

    Type(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
