package queue;

import java.util.Objects;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=1..n: a[i] != null && a[0] = null && a[n + 1] = null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]
public interface Queue {

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[n'] = element &&
    //       immutable(n) \\OK
    public void enqueue(Object element);

    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[1] = element &&
    //       forall i=2..n': a'[i] = a[i - 1] \\OK
    public void push(Object element);

    // Pre: n > 0
    // Post: R = a[1] && R != null && n' = n - 1 && forall i=1..n': a'[i] = a[i + 1] \\OK
    public Object dequeue();

    // Pre: n > 0
    // Post: R = a[n] && n' = n - 1 && immutable(n') \\OK
    public Object remove();

    // Pre: n > 0
    // Post: R = a[1] && n' = n && immutable(n) \\OK
    public Object element();

    // Pre: n > 0
    // Post: R = a[n] && n' = n && immutable(n) \\OK
    public Object peek();

    // Pre: true
    // Post: R = n && n' = n && immutable(n) \\OK
    /*public*/ int size();


    // Pre: true
    // Post: R = (n == 0) && n' = n && immutable(n) \\OK
    /*public*/ boolean isEmpty();

    //  Pred: true
    //  Post: n' = 0
    public void clear();

    //  Pred: true
    //  Post: a' = a : \forall i = 1..n-1 : a[i] != a[i + 1] && x < y < z: x = max(0..y - 1): a[x] != a[y]
    //  && z = min(y + 1..n + 1): a[y] != a[z] && \rfloor a[y] -> a'[y'] => a'[y' - 1] = a[x] && a'[y' + 1] = a[z]
    public void dedup();

}
