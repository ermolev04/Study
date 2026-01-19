package info.kgeorgiy.ja.ermolev.iterative;

import info.kgeorgiy.java.advanced.iterative.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;
    private boolean flag = false;

    /**
     * Default constructor for {@code IterativeParallelism}.
     * This constructor initialize {@code IterativeParallelism}.
     */
    public IterativeParallelism() {
        this.mapper = null;
    }

    /**
     * Constructor from mapper {@code ParallelMapper} for {@code IterativeParallelism}.
     * This constructor initialize {@code IterativeParallelism} from {@code ParallelMapper}.
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
        flag = true;
    }

    private static <T> List<List<T>> splitTasks(int threads, List<? extends T> values) {
        int size = values.size();
        if (size == 0) return Collections.emptyList();

        int subSize = size / threads;
        int shift = size % threads;

        List<List<T>> sublists = new ArrayList<>();
        int localShift = 0;
        int flag;
        for (int i = 0; i < threads; i++) {
            flag = 0;
            if (shift > 0) {
                shift--;
                flag = 1;
                localShift++;
            }
            sublists.add(new ArrayList<>(values.subList(subSize * i + localShift - flag, subSize * (i + 1) + localShift)));
        }

        return sublists;
    }

    private static <T> void paralleling(int threads, List<? extends T> values, List<List<T>> sublists, int[] results, Task<T> task) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < sublists.size(); i++) {
            final int index = i;
            final List<T> sublist = sublists.get(i);
            final int shift = (values.size() / threads) * i + Math.min(i, (values.size() % threads));
            Thread thread = new Thread(() -> task.execute(sublist, results, index, shift));
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    @FunctionalInterface
    private interface Task<T> {
        void execute(List<T> sublist, int[] results, int index, int shift);
    }

    private static <T> void longParalleling(int threads, List<? extends T> values, List<List<T>> sublists, long[] results, LongTask<T> task) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < sublists.size(); i++) {
            final int index = i;
            final List<T> sublist = sublists.get(i);
            final int shift = (values.size() / threads) * i + Math.min(i, values.size() % threads);
            Thread thread = new Thread(() -> task.execute(sublist, results, index, shift));
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    @FunctionalInterface
    private interface LongTask<T> {
        void execute(List<T> sublist, long[] results, int index, int shift);
    }

    /**
     * Find index of first Max element in list values
     *
     * @param threads    count of threads
     * @param values     list of data
     * @param comparator comporator for comparing elements T
     * @return first index of Max element
     * @throws InterruptedException If we trying interrupt threads.
     */
    @Override
    public <T> int argMax(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        int threadCount = Math.min(threads, values.size());
        List<List<T>> sublists = splitTasks(threadCount, values);
        int[] maxInd = new int[sublists.size()];

        if (flag) {
            List<Integer> results = mapper.map(sublist -> {
                int localInd = 0;

                T localMax = sublist.isEmpty() ? null : sublist.get(localInd);
                for (int j = 1; j < sublist.size(); j++) {
                    if (comparator.compare(sublist.get(j), localMax) > 0) {
                        localInd = j;
                        localMax = sublist.get(j);
                    }
                }
                return localInd;
            }, sublists);

            for (int i = 0; i < results.size(); i++) {
                maxInd[i] = results.get(i) + (values.size() / threadCount) * i + Math.min(i, values.size() % threadCount);
            }
        } else {
            paralleling(threadCount, values, sublists, maxInd, (sublist, results, index, shift) -> {
                int localInd = 0;
                T localMax = sublist.isEmpty() ? null : sublist.get(localInd);
                for (int j = 1; j < sublist.size(); j++) {
                    if (comparator.compare(sublist.get(j), localMax) > 0) {
                        localInd = j;
                        localMax = sublist.get(j);
                    }
                }
                results[index] = shift + localInd;
            });
        }
        int maxIndex = -1;
        T localMax;
        if (maxInd.length > 0) {
            maxIndex = maxInd[0];
            localMax = values.get(maxIndex);
        } else {
            return maxIndex;
        }

        for (int i = 1; i < maxInd.length; i++) {
            if (comparator.compare(values.get(maxInd[i]), localMax) > 0) {
                maxIndex = maxInd[i];
                localMax = values.get(maxInd[i]);
            }
        }
        return maxIndex;
    }

    /**
     * Find index of first Min element in list values
     *
     * @param threads    count of threads
     * @param values     list of data
     * @param comparator comporator for comparing elements T
     * @return first index of Min element
     * @throws InterruptedException If we trying interrupt threads.
     */
    @Override
    public <T> int argMin(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        return argMax(threads, values, comparator.reversed());
    }

    /**
     * Find index of first predicate element in list values
     *
     * @param threads   count of threads
     * @param values    list of data
     * @param predicate predicate for find elements T
     * @return first index of predicate element
     * @throws InterruptedException If we trying interrupt threads.
     */
    @Override
    public <T> int indexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        int threadCount = Math.min(threads, values.size());
        List<List<T>> sublists = splitTasks(threadCount, values);
        int[] indices = new int[sublists.size()];
        Arrays.fill(indices, -1);

        if (mapper != null) {
            // :NOTE: copy-paste
            List<Integer> results = mapper.map(sublist -> {
                for (int j = 0; j < sublist.size(); j++) {
                    if (predicate.test(sublist.get(j))) {
                        return j;
                    }
                }
                return -1;
            }, sublists);

            for (int i = 0; i < results.size(); i++) {
                if (results.get(i) != -1) {
                    indices[i] = results.get(i) + (values.size() / threadCount) * i + Math.min(i, values.size() % threadCount);
                }
            }
        } else {
            paralleling(threadCount, values, sublists, indices, (sublist, results, index, shift) -> {
                for (int j = 0; j < sublist.size(); j++) {
                    if (predicate.test(sublist.get(j))) {
                        results[index] = shift + j;
                        break;
                    }
                }
            });
        }


        int firstIndex = values.size();
        for (int index : indices) {
            if (index != -1 && index < firstIndex) {
                firstIndex = index;
            }
        }
        return firstIndex == values.size() ? -1 : firstIndex;
    }

    /**
     * Find index of last predicate element in list values
     *
     * @param threads   count of threads
     * @param values    list of data
     * @param predicate predicate for find elements T
     * @return last index of predicate element
     * @throws InterruptedException If we trying interrupt threads.
     */
    @Override
    public <T> int lastIndexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        int threadCount = Math.min(threads, values.size());
        List<List<T>> sublists = splitTasks(threadCount, values);
        int[] indices = new int[sublists.size()];
        Arrays.fill(indices, -1);

        if (flag) {
            List<Integer> results = mapper.map(sublist -> {
                int locResults = -1;
                for (int j = sublist.size() - 1; j >= 0; j--) {
                    if (predicate.test(sublist.get(j))) {
                        locResults = j;
                        break;
                    }
                }
                return locResults;
            }, sublists);

            for (int i = 0; i < results.size(); i++) {
                if (results.get(i) != -1) {
                    indices[i] = results.get(i) + ((values.size() / threadCount) * i + Math.min(i, values.size() % threadCount));
                }
            }
        } else {
            paralleling(threadCount, values, sublists, indices, (sublist, results, index, shift) -> {
                for (int j = sublist.size() - 1; j >= 0; j--) {
                    if (predicate.test(sublist.get(j))) {
                        results[index] = shift + j;
                        break;
                    }
                }
            });
        }


        int lastIndex = -1;
        for (int index : indices) {
            if (index != -1 && index > lastIndex) {
                lastIndex = index;
            }
        }
        return lastIndex;
    }


    /**
     * Find sum of index of all predicate element in list values
     *
     * @param threads   count of threads
     * @param values    list of data
     * @param predicate predicate for find elements T
     * @return sum of index of all predicate element
     * @throws InterruptedException If we tryied interrupt threads.
     */
    @Override
    public <T> long sumIndices(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        int threadCount = Math.min(threads, values.size());
        List<List<T>> sublists = splitTasks(threadCount, values);
        long[] sums = new long[threadCount];

        if (flag) {
            List<List<Long>> results = mapper.map(sublist -> {
                long localSum = 0, count = 0;
                for (int j = 0; j < sublist.size(); j++) {
                    if (predicate.test(sublist.get(j))) {
                        localSum += j;
                        count++;
                    }
                }
                return List.of(localSum, count);
            }, sublists);

            for (int i = 0; i < results.size(); i++) {
                long localSum = results.get(i).get(0);
                long count = results.get(i).get(1);
                long shift = ((values.size() / threadCount) * i + Math.min(i, values.size() % threadCount));
                sums[i] = localSum + count * shift;
            }
        } else {
            longParalleling(threadCount, values, sublists, sums, (sublist, results, index, shift) -> {
                long localSum = 0;
                for (int j = 0; j < sublist.size(); j++) {
                    if (predicate.test(sublist.get(j))) {
                        localSum += (long) shift + j;
                    }
                }
                results[index] = localSum;
            });
        }

        long totalSum = 0;
        for (long s : sums) {
            totalSum += s;
        }
        return totalSum;
    }
}
