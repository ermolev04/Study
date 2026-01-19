package info.kgeorgiy.ja.ermolev.lambda;

import info.kgeorgiy.java.advanced.lambda.EasyLambda;
import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

// :NOTE: много копипасты
public class Lambda implements EasyLambda {
    private static class BinaryTreeSpliterator<T> implements Spliterator<T> {
        private final Deque<Trees.Binary<T>> stack = new ArrayDeque<>();

        BinaryTreeSpliterator(Trees.Binary<T> tree) {
            stack.push(tree);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (!stack.isEmpty()) {
                Trees.Binary<T> node = stack.pop();
                // :NOTE: pattern matching for record
                if (node instanceof Trees.Leaf) {
                    T value = ((Trees.Leaf<T>) node).value();
                    action.accept(value);
                    return true;
                } else if (node instanceof Trees.Binary.Branch) {
                    Trees.Binary.Branch<T> branch = (Trees.Binary.Branch<T>) node;
                    stack.push(branch.right());
                    stack.push(branch.left());
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            if (stack.isEmpty()) {
                return null;
            }
            Trees.Binary<T> node = stack.pop();
            if (node instanceof Trees.Leaf<T> leaf) {
                return new BinaryTreeSpliterator<>(leaf);
            } else if (node instanceof Trees.Binary.Branch) {
                Trees.Binary.Branch<T> branch = (Trees.Binary.Branch<T>) node;
                stack.push(branch.right());
                return new BinaryTreeSpliterator<>(branch.left());
            }
            return null;
        }

        @Override
        public long estimateSize() {
            if (stack.peek() instanceof Trees.Leaf<T>) {
                return 1;
            }
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            if (stack.peek() instanceof Trees.Leaf<T>) {
                return ORDERED | IMMUTABLE | SIZED | SUBSIZED;
            }
            return IMMUTABLE | ORDERED;
        }
    }

    private static class SizedBinaryTreeSpliterator<T> implements Spliterator<T> {
        private final Deque<Trees.SizedBinary<T>> stack = new ArrayDeque<>();
        private final int size;

        SizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
            stack.push(tree);
            size = tree.size();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (!stack.isEmpty()) {
                Trees.SizedBinary<T> node = stack.pop();
                if (node instanceof Trees.Leaf) {
                    Trees.Leaf<T> leaf = (Trees.Leaf<T>) node;
                    action.accept(leaf.value());
                    return true;
                } else if (node instanceof Trees.SizedBinary.Branch<T> branch) {
                    stack.push(branch.right());
                    stack.push(branch.left());
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            if (stack.isEmpty()) {
                return null;
            }
            Trees.SizedBinary<T> node = stack.pop();
            if (node instanceof Trees.Leaf<T> leaf) {
                return new SizedBinaryTreeSpliterator<T>(leaf);
            } else if (node instanceof Trees.SizedBinary.Branch<T> branch) {
                stack.push(branch.right());
                return new SizedBinaryTreeSpliterator<T>(branch.left());
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return size;
        }

        @Override
        public int characteristics() {
            return ORDERED | IMMUTABLE | SIZED | SUBSIZED;
        }
    }

    private static class NaryTreeSpliterator<T> implements Spliterator<T> {
        private Deque<Trees.Nary<T>> stack = new ArrayDeque<>();

        NaryTreeSpliterator(Trees.Nary<T> tree) {
            stack.push(tree);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            while (!stack.isEmpty()) {
                Trees.Nary<T> node = stack.pop();
                if (node instanceof Trees.Leaf) {
                    Trees.Leaf<T> leaf = (Trees.Leaf<T>) node;
                    action.accept(leaf.value());
                    return true;
                } else if (node instanceof Trees.Nary.Node) {
                    List<Trees.Nary<T>> children = ((Trees.Nary.Node<T>) node).children();
                    for (int i = children.size() - 1; i >= 0; i--) {
                        stack.push(children.get(i));
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            if (stack.isEmpty()) {
                return null;
            }
            Trees.Nary<T> node = stack.pollLast();
            if (node instanceof Trees.Leaf<T> leaf) {
                return new NaryTreeSpliterator<T>(leaf);
            } else if (node instanceof Trees.Nary.Node) {
                List<Trees.Nary<T>> children = ((Trees.Nary.Node<T>) node).children();
                int mid = children.size() / 2;

                List<Trees.Nary<T>> leftList = children.subList(0, mid);
                List<Trees.Nary<T>> rightList = children.subList(mid, children.size());
                stack = new ArrayDeque<>();
                stack.addFirst(new Trees.Nary.Node<T>(rightList));


                return new NaryTreeSpliterator<T>(new Trees.Nary.Node<>(leftList));
            }
            return null;
        }

        @Override
        public long estimateSize() {
            if (!stack.isEmpty()) {
                return 0;
            }
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            if (stack.peek() instanceof Trees.Leaf<T>) {
                return ORDERED | IMMUTABLE | SIZED | SUBSIZED;
            }
            return ORDERED | IMMUTABLE;
        }
    }

    @Override
    public <T> Spliterator<T> binaryTreeSpliterator(Trees.Binary<T> tree) {
        return new BinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> sizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        return new SizedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> naryTreeSpliterator(Trees.Nary<T> tree) {
        return new NaryTreeSpliterator<>(tree);
    }


    @Override
    public <T> Collector<T, ?, Optional<T>> first() {
        return Collectors.reducing((a, b) -> a);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> last() {
        return Collectors.reducing((a, b) -> b);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> middle() {
        return Collector.of(
                ArrayList<T>::new,
                ArrayList::add,
                (arr1, arr2) -> {
                    arr1.addAll(arr2);
                    return arr1;
                },
                arr -> {
                    if (arr.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(arr.get(arr.size() / 2));
                }
        );
    }

    @Override
    public Collector<CharSequence, ?, String> commonPrefix() {
        return Collector.of(
                ArrayList<String>::new,
                (arr, data) -> arr.add(data.toString()),
                (arr1, arr2) -> {
                    arr1.addAll(arr2);
                    return arr1;
                },
                arr -> {
                    if (arr.isEmpty()) {
                        return "";
                    }
                    return checkAll(arr);
                }
        );
    }

    private String checkAll(ArrayList<String> arr) {
        StringBuilder buf = new StringBuilder();
        if (arr == null || arr.isEmpty()) {
            return "";
        }
        for (String str : arr) {
            if (str == null || str.isEmpty()) {
                return "";
            }
        }
        int index = 0;
        while (true) {
            char eta;
            if (arr.get(0).length() > index) {
                eta = arr.get(0).charAt(index);
            } else {
                return buf.toString();
            }
            for (String str : arr) {
                if (str.length() <= index || str.charAt(index) != eta) {
                    return buf.toString();
                }
            }
            buf.append(eta);
            index++;
        }
    }

    @Override
    public Collector<CharSequence, ?, String> commonSuffix() {
        return Collector.of(
                ArrayList<String>::new,
                (arr, data) -> arr.add(data.toString()),
                (arr1, arr2) -> {
                    arr1.addAll(arr2);
                    return arr1;
                },
                arr -> {
                    if (arr.isEmpty()) {
                        return "";
                    }
                    return checkAllSuf(arr);
                }
        );
    }

    private String checkAllSuf(ArrayList<String> arr) {
        StringBuilder buf = new StringBuilder();
        if (arr == null || arr.isEmpty()) {
            return "";
        }
        for (String str : arr) {
            if (str == null || str.isEmpty()) {
                return "";
            }
        }
        int index = 0;
        while (true) {
            char eta;
            if (arr.get(0).length() - index > 0) {
                eta = arr.get(0).charAt(arr.get(0).length() - 1 - index);
            } else {
                return buf.reverse().toString();
            }
            for (String str : arr) {
                if (str.length() - index <= 0 || str.charAt(str.length() - 1 - index) != eta) {
                    return buf.reverse().toString();
                }
            }
            buf.append(eta);
            index++;
        }
    }
}
