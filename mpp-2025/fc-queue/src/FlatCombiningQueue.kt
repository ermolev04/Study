import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * @author Ermolev Mihail
 */
class FlatCombiningQueue<E> : Queue<E> {
    private val queue = ArrayDeque<E>() // sequential queue
    private val combinerLock = AtomicBoolean(false) // unlocked initially
    private val tasksForCombiner = AtomicReferenceArray<Any?>(TASKS_FOR_COMBINER_SIZE)

    override fun enqueue(element: E) {
        if (combinerLock.compareAndSet(false, true)) {
            try {
                queue.addLast(element)
                processPendingTasks()
            } finally {
                combinerLock.set(false)
            }
            return
        }

        newOp<Unit>(element)
    }

    override fun dequeue(): E? {
        if (combinerLock.compareAndSet(false, true)) {
            return try {
                val result = queue.removeFirstOrNull()
                processPendingTasks()
                result
            } finally {
                combinerLock.set(false)
            }
        }

        return newOp(Dequeue)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> newOp(op: Any?): T {
        val cellIndex = ThreadLocalRandom.current().nextInt(tasksForCombiner.length())

        while (!tasksForCombiner.compareAndSet(cellIndex, null, op)) {
            Thread.yield()
        }

        while (true) {
            val cell = tasksForCombiner.get(cellIndex)

            if (cell is Result<*>) {
                tasksForCombiner.set(cellIndex, null)
                return cell.value as T
            }

            if (!combinerLock.get() && combinerLock.compareAndSet(false, true)) {
                try {
                    processPendingTasks()
                } finally {
                    combinerLock.set(false)
                }
            }

            Thread.yield()
        }
    }

    private fun processPendingTasks() {
        for (i in 0 until tasksForCombiner.length()) {
            val op = tasksForCombiner.get(i)
            if (op != null && op !is Result<*>) {
                if (op === Dequeue) {
                    tasksForCombiner.set(i, Result(queue.removeFirstOrNull()))
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val element = op as E
                    queue.addLast(element)
                    tasksForCombiner.set(i, Result(Unit))
                }
            }
        }
    }
}


private const val TASKS_FOR_COMBINER_SIZE = 3 // Do not change this constant!

private object Dequeue

private class Result<V>(
    val value: V
)
