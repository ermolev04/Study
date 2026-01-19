import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.*

/**
 * @author Ermolev Mikhail
 */
open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()

    // TODO: Try to optimize concurrent push and pop operations,
    // TODO: synchronizing them in an `rendezvousSlots` cell.
    private val rendezvousSlots = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushWithElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushWithElimination(element: E): Boolean {
        val randomIndex = ThreadLocalRandom.current().nextInt(ELIMINATION_ARRAY_SIZE)
        if (rendezvousSlots.compareAndSet(randomIndex, CELL_STATE_EMPTY, element)) {
            for (i in 0..ELIMINATION_WAIT_CYCLES) {
                if (rendezvousSlots.get(randomIndex) == CELL_STATE_RETRIEVED) {
                    if (rendezvousSlots.compareAndSet(randomIndex, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) {
                        return true
                    }
                }
            }
            if (rendezvousSlots.compareAndSet(randomIndex, element, CELL_STATE_EMPTY)) {
                return false
            } else {
                rendezvousSlots.set(randomIndex, CELL_STATE_EMPTY)
                return true
            }
        }
        return false
    }

    override fun pop(): E? = tryPopWithElimination() ?: stack.pop()

    private fun tryPopWithElimination(): E? {
        val randomIndex = ThreadLocalRandom.current().nextInt(ELIMINATION_ARRAY_SIZE)
        val supElement = rendezvousSlots.get(randomIndex)
        if (supElement != CELL_STATE_EMPTY && supElement != CELL_STATE_RETRIEVED && rendezvousSlots.compareAndSet(randomIndex, supElement, CELL_STATE_RETRIEVED)) {
            @Suppress("UNCHECKED_CAST")
            return supElement as E?
        }
        return null
    }

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 3 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        // Initially, all cells are in EMPTY state.
        private val CELL_STATE_EMPTY = null

        // `tryPopElimination()` moves the cell state
        // to `RETRIEVED` if the cell contains an element.
        private val CELL_STATE_RETRIEVED = Any()
    }
}
