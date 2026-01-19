package info.kgeorgiy.ja.ermolev.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final TaskQueue queue;

    /**
     * Constructor from threads for {@code ParallelMapperImpl}.
     * This constructor initialize with threads {@code ParallelMapperImpl}.
     */
    public ParallelMapperImpl(int threads) {
        queue = new TaskQueue();
        workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                try {
                    while (true) {
                        Task task = queue.getTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                }
            });
            worker.start();
            workers.add(worker);
        }
    }


    /**
     * Solve function for all arguments
     *
     * @param f function
     * @param args list of arguments
     * @throws InterruptedException If we trying interrupt threads.
     * @return List of function results
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultList<R> resultCollector = new ResultList<>(args.size());

        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            queue.addTask(new Task(() -> {
                try {
                    R result = f.apply(args.get(index));
                    resultCollector.addResult(index, result);
                } catch (RuntimeException e) {
                    resultCollector.addException(e);
                }
            }));
        }

        return resultCollector.awaitResults();
    }


    /**
     * Closed all threads
     */
    @Override
    public void close() {
        queue.close();
        for (Thread worker : workers) {
            worker.interrupt();
        }
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
