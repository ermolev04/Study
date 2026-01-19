package queue;

import java.util.Objects;
import java.util.function.Predicate;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=0..n: a[i] != null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]

public class ArrayQueueModule {
    private static int start = 0, end = 0, size = 0;
    private static Object[] elements = new Object[5];

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[n'] = element &&
    //       immutable(n) \\OK
    public static void enqueue(Object element) {
        Objects.requireNonNull(element);
        elements[end] = element;
        end = (end + 1) % elements.length;
        size++;
        ensureCapacity(end);
    }

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[1] = element &&
    //       forall i=2..n': a'[i] = a[i - 1] \\OK
    public static void push(Object element) {
        start = (elements.length + start - 1) % elements.length;
        Objects.requireNonNull(element);
        ensureCapacity(end % elements.length);
        elements[start] = element;
        size++;
    }

    // Pre: n > 0
    // Post: R = a[1] && n' = n - 1 && forall i=1..n': a'[i] = a[i + 1] \\OK
    public static Object dequeue() {
        assert !isEmpty();
        Object result = elements[start];
        elements[start] = null;
        start = (start + 1) % elements.length;
        size--;
        return result;
    }

    // Pre: n > 0
    // Post: R = a[n] && n' = n - 1 && immutable(n') \\OK
    public static Object remove() {
        assert !isEmpty();
        end = (elements.length + end - 1) % elements.length;
        Object result = elements[end];
        elements[end] = null;
        size--;
        return result;
    }

    // Pre: n > 0
    // Post: R = a[1] && n' = n && immutable(n) \\OK
    public static Object element() {
        assert !isEmpty();
        return elements[start];
    }

    // Pre: n > 0
    // Post: R = a[n] && n' = n && immutable(n) \\OK
    public static Object peek() {
        return elements[(elements.length + end - 1) % elements.length];
    }

    // Pre: true
    // Post: R = n && n' = n && immutable(n) \\OK
    public static int size() {
        return size;
    }

    // Pre: true
    // Post: R = (n == 0) && n' = n && immutable(n) \\OK
    public static boolean isEmpty() {
        return size == 0;
    }

    //  Pred: true
    //  Post: a' = new a[5] \\OK
    public static void clear() {
        elements = new Object[5];
        start = 0;
        end = 0;
        size = 0;
    }

    // Pre: predicate != null && pred = pred(x) ? true : false
    // Post: R = count ind forall i=1..n: test.(a[i]) == true && immutable(n) \\OK
    public static int countIf(Predicate<Object> pred) {
        int ans = 0;
        for (int i = start; i != end; i = (i + 1) % elements.length) {
            if (pred.test(elements[i])) {
                ans++;
            }
        }
        return ans;
    }

    private static void ensureCapacity(final int capacity) {
        if (start == capacity) {
            Object[] rightElements = new Object[elements.length * 2];
            System.arraycopy(elements, start, rightElements, 0, elements.length - start);
            System.arraycopy(elements, 0, rightElements, elements.length - start, end);
            start = 0;
            end = elements.length;
            elements = rightElements;
        }
    }
}
