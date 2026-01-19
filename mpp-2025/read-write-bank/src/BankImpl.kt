import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Bank implementation.
 *
 * Thread-safe implementation using per-account ReentrantReadWriteLocks,
 * two-phase locking, and hierarchical locking to avoid deadlocks.
 *
 * @author Ermolev Mihail
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val accountsCount: Int
        get() = accounts.size

    /**
     * Чтение баланса одного счёта.
     * Используем read-lock для конкретного счёта.
     */
    override fun amount(id: Int): Long {
        val account = accounts[id]
        val readLock = account.lock.readLock()
        readLock.lock()
        try {
            return account.amount
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Суммарный баланс по всем счетам.
     * Берём read-lock на все счета в фиксированном порядке (по индексу),
     * чтобы обеспечить линейризуемость и избежать дедлоков.
     */
    override val totalAmount: Long
        get() {
            for (account in accounts) {
                account.lock.readLock().lock()
            }
            try {
                var sum = 0L
                for (account in accounts) {
                    sum += account.amount
                }
                return sum
            } finally {
                for (i in accounts.indices.reversed()) {
                    accounts[i].lock.readLock().unlock()
                }
            }
        }

    /**
     * Пополнение одного счёта.
     * Используем write-lock конкретного счёта.
     */
    override fun deposit(id: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }

        val account = accounts[id]
        val writeLock = account.lock.writeLock()
        writeLock.lock()
        try {
            check(amount <= Bank.MAX_AMOUNT && account.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }
            account.amount += amount
            return account.amount
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Снятие средств с одного счёта.
     * Используем write-lock конкретного счёта.
     */
    override fun withdraw(id: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[id]
        val writeLock = account.lock.writeLock()
        writeLock.lock()
        try {
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            return account.amount
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Перевод между двумя счетами.
     * Используем иерархическую блокировку: сначала блокируем аккаунт
     * с меньшим индексом, потом с большим, всегда в таком порядке.
     */
    override fun transfer(fromId: Int, toId: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromId != toId) { "fromId == toId" }
        val firstId: Int
        val secondId: Int
        if (fromId < toId) {
            firstId = fromId
            secondId = toId
        } else {
            firstId = toId
            secondId = fromId
        }

        val firstLock = accounts[firstId].lock.writeLock()
        val secondLock = accounts[secondId].lock.writeLock()

        firstLock.lock()
        secondLock.lock()
        try {
            val from = accounts[fromId]
            val to = accounts[toId]

            check(amount <= from.amount) { "Underflow" }
            check(amount <= Bank.MAX_AMOUNT && to.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }

            from.amount -= amount
            to.amount += amount
        } finally {
            secondLock.unlock()
            firstLock.unlock()
        }
    }

    /**
     * Консолидация: списать все средства с fromIds и зачислить их на toId.
     * Блокируем все участвующие счета в иерархическом порядке (по id)
     * с write-lock, чтобы избежать дедлоков и обеспечить линейризуемость.
     */
    override fun consolidate(fromIds: List<Int>, toId: Int) {
        require(fromIds.isNotEmpty()) { "empty fromIds" }
        require(fromIds.distinct() == fromIds) { "duplicates in fromIds" }
        require(toId !in fromIds) { "toId in fromIds" }

        val allIds = (fromIds + toId).sorted()
        val lockedIds = mutableListOf<Int>()

        try {
            for (id in allIds) {
                accounts[id].lock.writeLock().lock()
                lockedIds += id
            }

            val fromList = fromIds.map { accounts[it] }
            val to = accounts[toId]

            val amount = fromList.sumOf { it.amount }
            check(to.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }

            for (from in fromList) {
                from.amount = 0
            }
            to.amount += amount
        } finally {
            for (id in lockedIds.asReversed()) {
                accounts[id].lock.writeLock().unlock()
            }
        }
    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        var amount: Long = 0

        /**
         * Read-Write lock for this account.
         */
        val lock = ReentrantReadWriteLock()
    }
}
