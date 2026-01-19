import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * @author TODO: Ermolev Mihail
 */
@OptIn(ExperimentalAtomicApi::class)
class BankImpl(override val accountsCount: Int) : Bank {
    /**
     * An array of accounts.
     * Account instances here are never reused (there is no ABA).
     */
    private val accounts = AtomicArray<Account>(accountsCount) { Account(0) }

    private fun account(id: Int) = accounts.loadAt(id)

    override fun amount(id: Int): Long {
        while (true) {
            val account = account(id)
            if (!account.invokeOperation()) return account.amount
        }
    }

    override val totalAmount: Long
        get() {
            /**
             * This operation requires atomic read of all accounts, thus it creates an operation descriptor.
             * Operation's invokeOperation method acquires all accounts, computes the total amount, and releases
             * all accounts. This method returns the result.
             */
            val op = TotalAmountOp()
            op.invokeOperation()
            return op.sum
        }

    override fun deposit(id: Int, amount: Long): Long { // First, validate method per-conditions
        require(amount > 0) { "Invalid amount: $amount" }
        check(amount <= MAX_AMOUNT) { "Overflow" }
        while (true) {
            val account = account(id)
            if (account.invokeOperation()) continue
            check(account.amount + amount <= MAX_AMOUNT) { "Overflow" }
            val updated = Account(account.amount + amount)
            if (accounts.compareAndSetAt(id, account, updated)) return updated.amount
        }
    }

    override fun withdraw(id: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }

        while (true) {
            val account = account(id)
            if (account.invokeOperation()) continue

            if (account.amount - amount < 0) {
                throw IllegalStateException("Underflow")
            }
            val updated = Account(account.amount - amount)
            if (accounts.compareAndSetAt(id, account, updated)) {
                return updated.amount
            }
        }
    }

    override fun transfer(fromId: Int, toId: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromId != toId) { "fromId == toId" }
        check(amount <= MAX_AMOUNT) { "Underflow/overflow" }
        /**
         * This operation requires atomic read of two accounts, thus it creates an operation descriptor.
         * Operation's invokeOperation method acquires both accounts, computes the result of operation
         * (if a form of error message), and releases both accounts. This method throws the exception with
         * the corresponding message if needed.
         */
        val op = TransferOp(fromId, toId, amount)
        op.invokeOperation()
        op.errorMessage?.let { error(it) }
    }

    private fun acquire(id: Int, op: Op): AcquiredAccount? {
        while (true) {
            val cur = accounts.loadAt(id)

            if (cur is AcquiredAccount) {
                val otherOp = cur.op
                if (otherOp === op) return cur

                if (otherOp.completed) {
                    release(id, otherOp)
                } else {
                    otherOp.invokeOperation()
                }
                continue
            }

            if (op.completed) return null

            val acquired = AcquiredAccount(cur.amount, op)
            if (accounts.compareAndSetAt(id, cur, acquired)) {
                return acquired
            }
        }
    }




    private fun release(id: Int, op: Op) {
        assert(op.completed)
        val account = account(id)
        if (account is AcquiredAccount && account.op === op) {
            val updated = Account(account.newAmount)
            accounts.compareAndSetAt(id, account, updated)
        }
    }

    /**
     * Immutable account data structure.
     * @param amount Amount of funds in this account.
     */
    private open class Account(val amount: Long) {
        /**
         * Invokes operation that is pending on this account.
         * This implementation returns false (no pending operation), other implementations return true.
         */
        open fun invokeOperation(): Boolean = false
    }

    /**
     * Account that was acquired as a part of in-progress operation that spans multiple accounts.
     * @see acquire
     */
    private class AcquiredAccount(
        var newAmount: Long,
        val op: Op
    ) : Account(newAmount) {
        override fun invokeOperation(): Boolean {
            op.invokeOperation()
            return true
        }
    }

    /**
     * Abstract operation that acts on multiple accounts.
     */
    private abstract class Op {
        /**
         * True when operation has completed.
         */
        @Volatile
        var completed = false

        abstract fun invokeOperation()
    }

    /**
     * Descriptor for [totalAmount] operation.
     */
    private inner class TotalAmountOp : Op() {
        var sum = 0L

        override fun invokeOperation() {
            var localSum = 0L
            var acquired = 0
            val n = accountsCount

            while (acquired < n) {
                val acc = acquire(acquired, this) ?: break
                localSum += acc.amount
                acquired++
            }

            if (acquired == n) {
                this.sum = localSum
                completed = true
            }

            for (i in 0 until n) {
                release(i, this)
            }
        }
    }



    /**
     * Descriptor for [transfer] operation.
     */
    private inner class TransferOp(
        val fromId: Int,
        val toId: Int,
        val amount: Long
    ) : Op() {
        var errorMessage: String? = null

        override fun invokeOperation() {
            val from: AcquiredAccount?
            val to: AcquiredAccount?

            if (fromId < toId) {
                from = acquire(fromId, this)
                to = acquire(toId, this)
            } else {
                to = acquire(toId, this)
                from = acquire(fromId, this)
            }

            if (from != null && to != null) {
                when {
                    amount > from.amount ->
                        errorMessage = "Underflow"
                    to.amount + amount > MAX_AMOUNT ->
                        errorMessage = "Overflow"
                    else -> {
                        from.newAmount = from.amount - amount
                        to.newAmount = to.amount + amount
                    }
                }
                completed = true
            }

            if (fromId < toId) {
                release(toId, this)
                release(fromId, this)
            } else {
                release(fromId, this)
                release(toId, this)
            }
        }
    }

}
