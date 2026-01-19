import java.util.concurrent.atomic.AtomicReference
/**
 * @author TODO: Ermolev Mihail
 */
@Suppress("UNCHECKED_CAST")
class AtomicArray<E>(size: Int, initialValue: E) {
    private val a: Array<Support<E>> = Array(size) { Support(initialValue) }

    fun get(index: Int): E = a[index].value

    fun set(index: Int, value: E) {
        a[index].value = value
    }

    fun cas(index: Int, expected: E, update: E): Boolean =
        a[index].cas(expected, update)

    fun cas2(
        index1: Int, expected1: E, update1: E,
        index2: Int, expected2: E, update2: E
    ): Boolean {
        while (true) {
            if (index1 == index2) {
                return if (expected1 == expected2) cas(index1, expected1, update2) else false
            }

            val firstIndex = if (index1 <= index2) index1 else index2
            val secondIndex = if (index1 <= index2) index2 else index1

            val firstExpected = if (index1 <= index2) expected1 else expected2
            val firstUpdate = if (index1 <= index2) update1 else update2

            val secondExpected = if (index1 <= index2) expected2 else expected1
            val secondUpdate = if (index1 <= index2) update2 else update1

            val descriptor = CAS2(
                a[firstIndex], firstExpected, firstUpdate,
                a[secondIndex], secondExpected, secondUpdate
            )

            if (a[firstIndex].cas(firstExpected, descriptor)) {
                descriptor.complete()
                return descriptor.status.value === Status.SUCCESS
            } else if (a[firstIndex].value != firstExpected) {
                return false
            }
        }
    }
}

class Support<T>(initial: T) {
    val reference = AtomicReference<Any?>(initial)

    var value: T
        get() {
            while (true) {
                val cur = reference.get()
                when (cur) {
                    is CAS2<*, *> -> cur.complete()
                    is RDCSS<*, *> -> cur.complete()
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        return cur as T
                    }
                }
            }
        }
        set(newValue) {
            while (true) {
                val cur = reference.get()
                when (cur) {
                    is CAS2<*, *> -> cur.complete()
                    is RDCSS<*, *> -> cur.complete()
                    else -> {
                        if (reference.compareAndSet(cur, newValue)) return
                    }
                }
            }
        }

    fun cas(expect: Any?, update: Any?): Boolean {
        while (true) {
            val cur = reference.get()
            when {
                cur is CAS2<*, *> -> cur.complete()
                cur is RDCSS<*, *> -> cur.complete()
                cur === expect -> {
                    if (reference.compareAndSet(expect, update)) return true
                }
                else -> return false
            }
        }
    }
}

class RDCSS<A, B>(
    val a: Support<A>, val expectA: Any?, val updateA: Any?,
    val b: Support<B>, val expectB: Any?
) {
    val outcome = Support(Status.UNDECIDED)

    fun complete() {
        if (b.value === expectB) {
            outcome.reference.compareAndSet(Status.UNDECIDED, Status.SUCCESS)
        } else {
            outcome.reference.compareAndSet(Status.UNDECIDED, Status.FAIL)
        }

        val finalValue = if (outcome.value === Status.SUCCESS) updateA else expectA
        a.reference.compareAndSet(this, finalValue)
    }
}

class CAS2<A, B>(
    private val a: Support<A>, private val expectA: A, private val updateA: A,
    private val b: Support<B>, private val expectB: B, private val updateB: B
) {
    val status = Support(Status.UNDECIDED)

    fun complete() {
        if (b.reference.get() !== this) {
            val rdcss = RDCSS(b, expectB, this, status, Status.UNDECIDED)
            if (b.cas(expectB, rdcss)) {
                rdcss.complete()
                status.reference.compareAndSet(Status.UNDECIDED, Status.SUCCESS)
            } else {
                status.reference.compareAndSet(Status.UNDECIDED, Status.FAIL)
            }
        } else {
            status.reference.compareAndSet(Status.UNDECIDED, Status.SUCCESS)
        }

        if (status.value === Status.SUCCESS) {
            a.reference.compareAndSet(this, updateA)
            b.reference.compareAndSet(this, updateB)
        } else {
            a.reference.compareAndSet(this, expectA)
            b.reference.compareAndSet(this, expectB)
        }
    }
}

enum class Status {
    UNDECIDED,
    SUCCESS,
    FAIL
}
