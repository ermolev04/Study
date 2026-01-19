import java.util.concurrent.atomic.*

/**
 * Implementation of the Michael-Scott queue algorithm.
 *
 * @author Ermolev Mikhail
 */
class MichaelScottQueue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>

    init {
        val dummy = Node<E>(null)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    fun enqueue(element: E) {
        val newTail = Node(element)
        while(true) {
            val t = tail.get();
            if(t.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(t, newTail)
                return
            }
            else {
                tail.compareAndSet( t, t.next.get())
            }
        }
    }

    fun dequeue(): E? {
        while (true) {
            val h = head.get()
            val t = tail.get()
            val nextHead = h.next.get()
            if (h == head.get()) {
                if (h == t) {
                    if (nextHead == null)
                        return null
                    tail.compareAndSet(t, nextHead)
                } else {
                    val result = nextHead?.element
                    if (head.compareAndSet(h, nextHead)) {
                        nextHead?.element = null
                        return result
                    }
                }
            }
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    fun validate() {
        check(tail.get().next.get() == null) {
            "At the end of the execution, `tail.next` must be `null`"
        }
        check(head.get().element == null) {
            "At the end of the execution, the dummy node shouldn't store an element"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}
