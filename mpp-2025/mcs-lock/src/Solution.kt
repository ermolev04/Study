import java.util.concurrent.atomic.AtomicReference

class Solution(val env: Environment) : Lock<Solution.Node> {
    private val tail = AtomicReference<Node?>(null)

    override fun lock(): Node {
        val my = Node()
        my.locked.value = true
        my.next.value = null

        val pred = tail.getAndSet(my)
        if (pred != null) {
            pred.next.value = my

            while (my.locked.value) {
                env.park()
            }
        }
        return my
    }

    override fun unlock(node: Node) {
        if (node.next.value == null) {
            if (tail.compareAndSet(node, null)) {
                return
            }
            while (node.next.value == null) {
            }
        }

        val succ = node.next.value
        if (succ != null) {
            succ.locked.value = false
            env.unpark(succ.thread)
        }
    }

    class Node {
        val thread: Thread = Thread.currentThread()
        val locked = AtomicReference<Boolean>(false)
        val next = AtomicReference<Node?>(null)
    }
}
