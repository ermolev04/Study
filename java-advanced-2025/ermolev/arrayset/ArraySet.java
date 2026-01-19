package info.kgeorgiy.ja.ermolev.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {
    private final ArrayList<E> data;
    private final Comparator<? super E> comparator;


    private ArraySet(final List<E> data, final Comparator<? super E> comparator) {
        this.data = new ArrayList<>(data);
        this.comparator = comparator;
    }
    public ArraySet() {
        this(List.of());
    }

    public ArraySet(final Collection<E> data) {
        this(data, null);
    }

    public ArraySet(final Collection<E> data, final Comparator<? super E> comparator) {
        this.comparator = comparator;
        TreeSet<E> sup = new TreeSet<>(comparator);
        sup.addAll(data);
        this.data = new ArrayList<>(sup);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private int compare(E a, E b) {
        if (comparator == null) {
            return Comparator.<E>naturalOrder().compare(a, b);
        }
        return comparator.compare(a, b);
    }

    private SortedSet<E> cutSet(E fromElement, E toElement) {
        int l, r;
        if (fromElement == null) {
            l = 0;
        } else {
            l = findIndex(fromElement);
        }
        if (toElement == null) {
            r = data.size();
        } else {
            r = findIndex(toElement);
        }
        return new ArraySet<>(data.subList(l, r), comparator);
    }

    private int findIndex(E element) {
        int index = Collections.binarySearch(data, element, this::compare);
        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return cutSet(fromElement, toElement);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return cutSet(null, toElement);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return cutSet(fromElement, null);
    }

    @Override
    public E first() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object e) {
        return Collections.binarySearch(data, (E) Objects.requireNonNull(e), comparator) >= 0;
    }
}
