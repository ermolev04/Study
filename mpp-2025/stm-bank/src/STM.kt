import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/*
   Obstruction-free STM implementation.
   @author Ermolev Mihail
*/

/**
 * Atomic block.
 */
fun <T> atomic(block: TxScope.() -> T): T {
    while (true) {
        val transaction = Transaction()
        try {
            val result = block(transaction)
            if (transaction.commit()) return result
            transaction.abort()
        } catch (e: AbortException) {
            transaction.abort()
        }
    }
}

/**
 * Transactional operations are performed in this scope.
 */
abstract class TxScope {
    abstract fun <T> TxVar<T>.read(): T
    abstract fun <T> TxVar<T>.write(x: T): T
}

/**
 * Transactional variable.
 */
@OptIn(ExperimentalAtomicApi::class)
class TxVar<T>(initial: T)  {
    private val loc = AtomicReference(Loc(initial, initial, rootTx))

    /**
     * Opens this transactional variable in the specified transaction [tx] and applies
     * updating function [update] to it. Returns the updated value.
     */
    fun openIn(tx: Transaction, update: (T) -> T): T {
        while (true) {
            if (tx.status != TxStatus.ACTIVE) throw AbortException

            val curLoc = loc.load()
            val owner = curLoc.owner

            if (owner === tx) {
                val curValue = curLoc.newValue
                val updValue = update(curValue)
                val newLoc = Loc(curLoc.oldValue, updValue, tx)
                if (loc.compareAndSet(curLoc, newLoc)) return updValue
                continue
            }

            when (owner.status) {
                TxStatus.ACTIVE -> {
                    owner.abort()
                }

                TxStatus.COMMITTED -> {
                    val visible = curLoc.newValue
                    val updValue = update(visible)
                    val newLoc = Loc(visible, updValue, tx)
                    if (loc.compareAndSet(curLoc, newLoc)) return updValue
                }

                TxStatus.ABORTED -> {
                    val visible = curLoc.oldValue
                    val updValue = update(visible)
                    val newLoc = Loc(visible, updValue, tx)
                    if (loc.compareAndSet(curLoc, newLoc)) return updValue
                }
            }
        }
    }
}

/**
 * State of transactional value
 */
private class Loc<T>(
    val oldValue: T,
    val newValue: T,
    val owner: Transaction
)

private val rootTx = Transaction().apply { commit() }

/**
 * Transaction status.
 */
enum class TxStatus { ACTIVE, COMMITTED, ABORTED }

/**
 * Transaction implementation.
 */
@OptIn(ExperimentalAtomicApi::class)
class Transaction : TxScope() {
    private val _status = AtomicReference(TxStatus.ACTIVE)
    val status: TxStatus get() = _status.load()

    fun commit(): Boolean =
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.COMMITTED)

    fun abort() {
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED)
    }

    override fun <T> TxVar<T>.read(): T = openIn(this@Transaction) { it }
    override fun <T> TxVar<T>.write(x: T) = openIn(this@Transaction) { x }
}

/**
 * This exception is thrown when transaction is aborted.
 */
private object AbortException : Exception() {
    override fun fillInStackTrace(): Throwable = this
}