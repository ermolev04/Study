package queue;

import java.util.function.Predicate;

public class ArrayQueue extends AbstractQueue {
    private int start = 0, end = 0;
    private Object[] elements = new Object[5];
    @Override
    protected void localEnqueue(Object element) {
        this.elements[this.end] = element;
        this.end = (this.end + 1) % this.elements.length;
        ensureCapacity();
    }
    @Override
    protected void localPush(Object element) {
        this.start = (this.elements.length + this.start - 1) % this.elements.length ;
        ensureCapacity();
        this.elements[this.start] = element;
    }
    @Override
    protected void localDequeue() {
        this.elements[this.start] = null;
        this.start = (this.start + 1) % this.elements.length;
    }

    @Override
    protected void localRemove() {
        this.end = (this.elements.length + this.end - 1) % this.elements.length;
        this.elements[this.end] = null;
    }


    @Override
    protected Object localElement() {
        return this.elements[this.start];
    }

    @Override
    public Object peek() {
        return this.elements[(this.elements.length + this.end - 1) % this.elements.length];
    }

    @Override
    protected void localClear() {
        this.start = 0;
        this.end = 0;
        // :NOTE: Arrays.fill(null)
    }

    // Pre: predicate != null && pred = pred(x) ? true : false
    // Post: R = count ind forall i=1..n: test.(a[i]) == true && immutable(n) \\OK
    public int countIf(Predicate<Object> pred) {
        int ans = 0;
        for(int i = this.start; i != this.end; i = (i + 1) % this.elements.length) {
            if(pred.test(this.elements[i])){
                ans++;
            }
        }
        return ans;
    }
    private void ensureCapacity() {
        if (this.start == this.end){
            Object[] rightElements = new Object[this.elements.length * 2];
            System.arraycopy(this.elements, this.start, rightElements, 0, this.elements.length - this.start);
            System.arraycopy(this.elements, 0, rightElements, this.elements.length - this.start, this.end);
            this.start = 0; this.end = this.elements.length;
            this.elements = rightElements;
        }
    }
}
