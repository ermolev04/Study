import java.util.concurrent.atomic.*

/**
 * @author Ermolev Mihail
 *
 * TODO: Copy the code from `FAABasedQueueSimplified`
 * TODO: and implement the infinite array on a linked list
 * TODO: of fixed-size `Segment`s.
 */
class FAABasedQueue<E> : Queue<E> {
    private val head: AtomicReference<Segment>
    private val tail: AtomicReference<Segment>
    private val enqIdx = AtomicLong(0)
    private val deqIdx = AtomicLong(0)

    init {
        val dummy = Segment(0)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        while(true) {
            val curTail = tail.get()
            val i = enqIdx.getAndIncrement()
            val s = findSegment(curTail, i / SEGMENT_SIZE)
            if (curTail.id < i / SEGMENT_SIZE) {
                tail.compareAndSet(curTail, s)
            }
            if (s.cells.compareAndSet((i % SEGMENT_SIZE).toInt(), null, element)) {
                return
            }
        }
    }



    override fun dequeue(): E? {
        while (true) {
            if (!shouldTryToDequeue()) return null

            val curHead = head.get()
            val i = deqIdx.getAndIncrement()
            val segment = findSegment(curHead, i / SEGMENT_SIZE)

            if (curHead.id < segment.id) head.compareAndSet(curHead, segment)
            if (segment.cells.compareAndSet((i % SEGMENT_SIZE).toInt(), null, POISONED)) continue

            return segment.cells.getAndSet((i % SEGMENT_SIZE).toInt(), null) as E
        }
    }

    private fun shouldTryToDequeue(): Boolean {
        while(true) {
            val curDeqIdx = deqIdx.get()
            val curEnqIdx = enqIdx.get()
            if (curDeqIdx == deqIdx.get()) {
                return curDeqIdx < curEnqIdx
            }
        }
    }
}

private fun findSegment(curTail: Segment, n: Long) : Segment {
    var current = curTail

    for (i in curTail.id..<n) {
        val nextSegment = Segment(i + 1)
        current.next.compareAndSet(null, nextSegment)
        current = current.next.get()!!
    }

    return current
}

private class Segment(val id: Long) {
    val next = AtomicReference<Segment?>(null)
    val cells = AtomicReferenceArray<Any?>(SEGMENT_SIZE)
}

private val POISONED = Any()

// DO NOT CHANGE THIS CONSTANT
private const val SEGMENT_SIZE = 2
