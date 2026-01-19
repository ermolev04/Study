package queue;

import java.util.Objects;

public abstract class AbstractQueue implements Queue {
    protected int size;

    public void enqueue(Object element) {
        Objects.requireNonNull(element);
        localEnqueue(element);
        this.size++;
    }

    protected void localEnqueue(Object element) {
    }

    public void push(Object element) {
        Objects.requireNonNull(element);
        localPush(element);
        this.size++;
    }

    protected void localPush(Object element) {
    }

    public Object dequeue() {
        assert !isEmpty();
        Object result = element();
        localDequeue();
        this.size--;
        return result;
    }

    protected void localDequeue() {
    }

    public Object remove() {
        assert !isEmpty();
        Object result = peek();
        localRemove();
        this.size--;
        return result;
    }

    protected void localRemove() {
    }


    public Object element() {
        assert !isEmpty();
        return localElement();
    }
    // :NOTE: return null?

    protected Object localElement() {
        return null;
    }


    public Object peek() {
        return null;
    }

    ;


    public int size() {
        return this.size;
    }


    public boolean isEmpty() {
        return this.size == 0;
    }


    public void clear() {
        this.size = 0;
        localClear();
    }

    protected void localClear() {
    }

    public void dedup() {
        if (isEmpty()) {
            return;
        }
        int curSize = size;
        Object next, cur;
        cur = dequeue();
        enqueue(cur);
        for (int i = 1; i < curSize; i++) {
            next = dequeue();
            if (!next.equals(cur)) {
                cur = next;
                enqueue(cur);
            }
        }
    }

}
