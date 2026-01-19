@file:Suppress("DuplicatedCode", "FoldInitializerAndIfToElvis")

import java.util.concurrent.atomic.*

class MSQueueWithConstantTimeRemove<E> : QueueWithRemove<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>

    init {
        val dummy = Node<E>(element = null, prev = null)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        while (true) {
            val last = tail.get()
            val next = last.next.get()

            if (last !== tail.get()) continue

            if (next != null) {
                // Tail is behind; help advance.
                tail.compareAndSet(last, next)
                continue
            }

            val newNode = Node(element = element, prev = last)
            if (last.next.compareAndSet(null, newNode)) {
                // Best-effort tail swing; also help catch up if needed.
                tail.compareAndSet(last, newNode)
                advanceTail()

                // If the previous tail was removed, unlink it (now it is surely not the tail).
                if (last.extractedOrRemoved) {
                    last.helpUnlink()
                }
                return
            }
        }
    }

    private fun advanceTail() {
        while (true) {
            val t = tail.get()
            val n = t.next.get() ?: return
            tail.compareAndSet(t, n)
        }
    }

    override fun dequeue(): E? {
        while (true) {
            val firstHead = head.get()
            val first = firstHead.next.get() ?: return null
            val last = tail.get()

            if (firstHead !== head.get()) continue

            if (firstHead === last) {
                // Tail is behind (queue not empty because first != null), help advance it.
                tail.compareAndSet(last, first)
                continue
            }

            if (head.compareAndSet(firstHead, first)) {
                // `first` becomes the new dummy head.
                first.prev.set(null)

                // Now try to "claim" this node.
                // If it was already removed, restart (head already advanced, which is fine).
                if (!first.markExtractedOrRemoved()) {
                    continue
                }

                val res = first.element
                first.element = null
                return res
            }
        }
    }

    override fun remove(element: E): Boolean {
        // DO NOT CHANGE THIS CODE.
        var node = head.get()
        while (true) {
            val next = node.next.get()
            if (next == null) return false
            node = next
            if (node.element == element && node.remove()) return true
        }
    }

    override fun validate() {
        check(head.get().prev.get() == null) {
            "`head.prev` must be null"
        }
        check(tail.get().next.get() == null) {
            "tail.next must be null"
        }
        var node = head.get()
        while (true) {
            if (node !== head.get() && node !== tail.get()) {
                check(!node.extractedOrRemoved) {
                    "Removed node with element ${node.element} found in the middle of the queue"
                }
            }
            val nodeNext = node.next.get()
            if (nodeNext == null) break
            val nodeNextPrev = nodeNext.prev.get()
            check(nodeNextPrev != null) {
                "The `prev` pointer of node with element ${nodeNext.element} is `null`, while the node is in the middle of the queue"
            }
            check(nodeNextPrev == node) {
                "node.next.prev != node; `node` contains ${node.element}, `node.next` contains ${nodeNext.element}"
            }
            node = nodeNext
        }
    }

    private class Node<E>(
        var element: E?,
        prev: Node<E>?
    ) {
        val next = AtomicReference<Node<E>?>(null)
        val prev = AtomicReference(prev)

        private val _extractedOrRemoved = AtomicBoolean(false)
        val extractedOrRemoved get() = _extractedOrRemoved.get()

        fun markExtractedOrRemoved(): Boolean =
            _extractedOrRemoved.compareAndSet(false, true)

        /**
         * Best-effort physical unlink.
         * Key property: NEVER unlink using a stale predecessor.
         */
        fun helpUnlink() {
            while (true) {
                val p = prev.get() ?: return      // don't remove head
                val n = next.get() ?: return      // don't remove tail

                // If our prev changed while we were reading, restart.
                if (prev.get() !== p) continue

                // If predecessor is removed and not the head, help unlink it first.
                if (p.extractedOrRemoved && p.prev.get() != null) {
                    p.helpUnlink()
                    continue
                }

                if (p.next.compareAndSet(this, n)) {
                    // Only fix n.prev if it still points to a REMOVED node.
                    fixPrev(n, newPrev = p)
                    return
                }

                // CAS failed. If p doesn't point to us anymore, our `prev` is stale.
                if (p.next.get() !== this) {
                    val pp = p.prev.get() ?: return   // if p is head and doesn't point to us => we're not reachable
                    // Move our prev left and retry (helps avoid "removed node in the middle").
                    prev.compareAndSet(p, pp)
                }
                // else: p.next is still this, just retry
            }
        }

        private fun fixPrev(node: Node<E>, newPrev: Node<E>) {
            while (true) {
                val cur = node.prev.get()
                if (cur == null || cur === newPrev) return

                // Critical: don't override a correct/live predecessor.
                // Only rewrite if the current prev is already removed.
                if (!cur.extractedOrRemoved) return

                if (node.prev.compareAndSet(cur, newPrev)) return
            }
        }

        fun remove(): Boolean {
            // Phase 1: logical removal.
            if (!markExtractedOrRemoved()) return false

            // Phase 2: physical removal (retry until unlinked or we become head/tail).
            helpUnlink()
            return true
        }
    }
}
