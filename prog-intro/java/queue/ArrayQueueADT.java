package queue;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=0..n: a[i] != null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]
public class ArrayQueueADT {
    private int start = 0, end = 0, size = 0;
    private Object[] elements = new Object[5];


    //  Pred: true
    //  Post: R.n == 0 && R new \\OK
    public static ArrayQueueADT create() {
        return new ArrayQueueADT();
    }

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[n'] = element &&
    //       immutable(n) \\OK
    public static void enqueue(ArrayQueueADT queue, Object element) {
        Objects.requireNonNull(element);
        queue.elements[queue.end] = element;
        queue.end = (queue.end + 1) % queue.elements.length;
        queue.size++;
        ensureCapacity(queue, queue.end);
    }

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[1] = element &&
    //       forall i=2..n': a'[i] = a[i - 1] \\OK
    public static void push(ArrayQueueADT queue, Object element) {
        Objects.requireNonNull(element);
        queue.start = (queue.elements.length + queue.start - 1) % queue.elements.length ;
        ensureCapacity(queue, queue.end % queue.elements.length);
        queue.elements[queue.start] = element;
        queue.size++;
    }

    // Pre: true && predicate != null && predicate don't throw exception && pred.test return boolean
    // Post: R = count ind forall i=1..n: test.(a[i]) == true && immutable(n) \\OK
    public static Object dequeue(ArrayQueueADT queue) {
        assert !isEmpty(queue);
        Object result = queue.elements[queue.start];
        queue.elements[queue.start] = null;
        queue.start = (queue.start + 1) % queue.elements.length;
        queue.size--;
        return result;
    }

    // Pre: n > 0
    // Post: R = a[n] && n' = n - 1 && immutable(n') \\OK
    public static Object remove(ArrayQueueADT queue) {
        assert !isEmpty(queue);
        queue.end = (queue.elements.length + queue.end - 1) % queue.elements.length;
        Object result = queue.elements[queue.end];
        queue.elements[queue.end] = null;
        queue.size--;
        return result;
    }

    // Pre: n > 0
    // Post: R = a[1] && n' = n && immutable(n) \\OK
    public static Object element(ArrayQueueADT queue) {
        assert !isEmpty(queue);
        return queue.elements[queue.start];
    }

    // Pre: n > 0
    // Post: R = a[n] && n' = n && immutable(n) \\OK
    public static Object peek(ArrayQueueADT queue) {
        return queue.elements[(queue.elements.length + queue.end - 1) % queue.elements.length];
    }

    // Pre: true
    // Post: R = n && n' = n && immutable(n) \\OK
    public static int size(ArrayQueueADT queue) {
        return queue.size;
    }

    // Pre: true
    // Post: R = (n == 0) && n' = n && immutable(n) \\OK
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.size == 0;
    }

    //  Pred: true
    //  Post: a = new a[5] \\OK
    public static void clear(ArrayQueueADT queue) {
        queue.elements = new Object[5];
        queue.start = 0;
        queue.end = 0;
        queue.size = 0;
    }

    // Pre: predicate != null && pred = pred(x) ? true : false
    // Post: R = count ind forall i=1..n: test.(a[i]) == true && immutable(n) \\OK
    public static int countIf(ArrayQueueADT queue, Predicate<Object> pred) {
        int ans = 0;
        for(int i = queue.start; i != queue.end; i = (i + 1) % queue.elements.length) {
            if(pred.test(queue.elements[i])){
                ans++;
            }
        }
        return ans;
    }
    private static void ensureCapacity(ArrayQueueADT queue, final int capacity) {
        if (queue.start == capacity){
            Object[] rightElements = new Object[queue.elements.length * 2];
            System.arraycopy(queue.elements, queue.start, rightElements, 0, queue.elements.length - queue.start);
            System.arraycopy(queue.elements, 0, rightElements, queue.elements.length - queue.start, queue.end);
            queue.start = 0; queue.end = queue.elements.length;
            queue.elements = rightElements;
        }
    }
}
