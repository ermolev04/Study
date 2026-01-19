package info.kgeorgiy.ja.ermolev.iterative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultList<R> {
    private final List<R> results;
    private final List<RuntimeException> exceptions = new ArrayList<>();
    private int remaining;

    /**
     * Constructor from size for {@code ResultList}.
     * This constructor initialize with size {@code ResultList}.
     */
    ResultList(int size) {
        this.results = new ArrayList<>(Collections.nCopies(size, null));
        this.remaining = size;
    }

    /**
     * Add result of Task
     *
     * @param index index of task
     * @param result result of task
     */
    synchronized void addResult(int index, R result) {
        results.set(index, result);
        checkComplete();
    }

    /**
     * Add exception from Task
     *
     * @param e Exception from task
     */
    synchronized void addException(RuntimeException e) {
        exceptions.add(e);
        checkComplete();
    }

    private void checkComplete() {
        remaining--;
        if (remaining == 0) {
            notify();
        }
    }

    /**
     * Wait and return results. Can throw exception
     *
     * @return List of results and throw exception
     */
    // :NOTE: блокировка на одном объекте
    synchronized List<R> awaitResults() throws InterruptedException {
        while (remaining > 0) {
            wait();
        }
        if (!exceptions.isEmpty()) {
            RuntimeException e = new RuntimeException("ERROR: We find some exceptions!!!");
            exceptions.forEach(e::addSuppressed);
            throw e;
        }
        return results;
    }
}