package queue;

public class LinkedQueue extends AbstractQueue {
    private Node start, end;

    @Override
    public void localEnqueue(Object element) {
        if (isEmpty()) {
            firstElement(element);
        } else {
            this.end = new Node(element, null, this.end);
            this.end.prev.next = this.end;
        }
    }

    @Override
    protected void localPush(Object element) {
        if (isEmpty()) {
            firstElement(element);
        } else {
            this.start = new Node(element, this.start, null);
            this.start.next.prev = this.start;
        }
    }

    private void firstElement(Object element) {
        this.end = new Node(element, null, null);
        this.start = this.end;
    }

    @Override
    protected void localDequeue() {
        this.start = this.start.next;
        if (this.start != null) {
            this.start.prev = null;
        }
    }

    @Override
    protected void localRemove() {
        this.end = this.end.prev;
        if (this.end != null) {
            this.end.next = null;
        }
    }

    @Override
    protected Object localElement() {
        assert !isEmpty();
        return this.start.value;
    }

    @Override
    public Object peek() {
        return this.end.value;
    }


    @Override
    protected void localClear() {
        this.start = null;
        this.end = null;
    }

    private class Node {
        private final Object value;
        private Node next, prev;

        public Node(Object value, Node next, Node prev) {
            assert value != null;

            this.value = value;
            this.next = next;
            this.prev = prev;
        }
    }
}
