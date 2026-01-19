package prtest.primes;

import base.Selector;
import base.TestCounter;
import prtest.PrologScript;
import prtest.Rule;

import java.util.function.Consumer;

/**
 * Tests for
 * <a href="https://www.kgeorgiy.info/courses/paradigms/homeworks.html#prolog-map">Prolog Primes</a>
 * homework of <a href="https://www.kgeorgiy.info/courses/paradigms">Programming Paradigms</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public final class PrimesTest {
    // GCD
    private static long gcd(final long a, final long b) {
        return a == 0 ? b : gcd(b % a, a);
    }
    private static void gcd(final PrimesTester t) {
        t.testBinary("gcd", PrimesTest::gcd);
    }

    // LCM
    private static void lcm(final PrimesTester t) {
        t.testBinary("lcm", (a, b) -> a * b / gcd(a, b));
    }
    private static void gcdLcm(final PrimesTester t) {
        gcd(t);
        lcm(t);
    }

    // Nth
    private static final Rule NTH_PRIME = new Rule("nth_prime", 1 + 1);
    private static final Rule NTH = NTH_PRIME.func();
    private static final Rule NTH_REVERSE = NTH_PRIME.bind(0, PrologScript.V);

    private static void nth(final PrimesTester t, final int i) {
        t.assertResult(t.primes[i], NTH, i + 1);
        if (t.reversible) {
            t.assertResult(i + 1, NTH_REVERSE, t.primes[i]);
        }
    }

    private static void nth(final PrimesTester t) {
        for (int i = 0; i < 10; i++) {
            nth(t, i);
        }
        for (int i = 0; t.primes[i] * t.primes[i] < t.max * 10; i += 10) {
            nth(t, i);
        }
    }


    public static final Selector SELECTOR = new Selector(PrimesTest.class, "easy", "hard", "bonus")
            .variant("Primes", variant(t -> {}))
            .variant("Gcd", variant(PrimesTest::gcd))
            .variant("GcdLcm", variant(PrimesTest::gcdLcm))
            .variant("Nth", variant(PrimesTest::nth))
            ;

    private PrimesTest() {
    }

    public static void main(final String... args) {
        SELECTOR.main(args);
    }

    /* package-private */ static Consumer<TestCounter> variant(final Consumer<PrimesTester> check) {
        return counter -> {
            final int mode = counter.mode();
            final int max = (int) (1000 * Math.pow(100.0 / TestCounter.DENOMINATOR, mode));
            new PrimesTester(counter, max, mode > 0, check).test();
        };
    }
}
