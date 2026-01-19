import java.util.concurrent.locks.ReentrantLock

/**
 * Bank implementation.
 *
 * @author Ermolev Mihail
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }
    override val accountsCount: Int
        get() = accounts.size


    override fun amount(id: Int): Long {

    try {
        accounts[id].lock.lock()
        return accounts[id].amount
    } finally {
        accounts[id].lock.unlock()
    }

    }

    override val totalAmount: Long
        get() {
            for (account in accounts) {
                account.lock.lock()
            }
            try {
                return accounts.sumOf {
                    account ->
                    account.amount
                }
            } finally {
                for (account in accounts) {
                    account.lock.unlock()
                }
            }
        }

    override fun deposit(id: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        accounts[id].lock.lock()
        try {
            val account = accounts[id]
            check(!(amount > Bank.MAX_AMOUNT || account.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            account.amount += amount
            val ans = account.amount
            return ans
        } finally {
            accounts[id].lock.unlock()
        }

    }

    override fun withdraw(id: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        accounts[id].lock.lock()
        try {
            val account = accounts[id]
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            val ans = account.amount

            return ans
        } finally {
            accounts[id].lock.unlock()
        }

    }

    override fun transfer(fromId: Int, toId: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromId != toId) { "fromId == toId" }
        if(fromId > toId) {
            accounts[toId].lock.lock()
            accounts[fromId].lock.lock()
        } else {
            accounts[fromId].lock.lock()
            accounts[toId].lock.lock()
        }
        try {
            val from = accounts[fromId]
            val to = accounts[toId]
            check(amount <= from.amount) { "Underflow" }
            check(!(amount > Bank.MAX_AMOUNT || to.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            from.amount -= amount
            to.amount += amount
        } finally {
            accounts[fromId].lock.unlock()
            accounts[toId].lock.unlock()
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
        val lock = ReentrantLock()
    }
}