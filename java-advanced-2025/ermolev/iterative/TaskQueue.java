package info.kgeorgiy.ja.ermolev.iterative;

import java.util.ArrayDeque;
import java.util.Queue;

public class TaskQueue {
    private final Queue<Task> tasks = new ArrayDeque<>();
    private boolean isClosed = false;

    /**
     * Add task in Queue
     *
     * @param task task
     */
    synchronized void addTask(Task task) {
        if (isClosed) {
            throw new IllegalStateException("Queue is closed");
        }
        tasks.add(task);
        notify();
    }

    /**
     * Return task from Queue
     *
     * @return task
     */
    synchronized Task getTask() throws InterruptedException {
        while (tasks.isEmpty() && !isClosed) {
            wait();
        }
        return tasks.poll();
    }

    /**
     * Close this Queue
     */
    synchronized void close() {
        isClosed = true;
        notifyAll();
    }
}
