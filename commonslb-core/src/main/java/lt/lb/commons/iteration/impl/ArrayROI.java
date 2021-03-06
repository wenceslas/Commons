package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import java.util.Objects;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;

/**
 *
 * @author laim0nas100
 */
public class ArrayROI<T> extends BaseROI<T> implements ReadOnlyBidirectionalIterator<T> {

    protected final T[] array;

    public ArrayROI(T... array) {
        Objects.requireNonNull(array);
        this.array = array;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public T previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException("No previous value");
        }
        return setCurrent(array[--index]);
    }

    @Override
    public boolean hasNext() {
        return 1 + index < array.length;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No previous value");
        }
        return setCurrent(array[++index]);
    }

}
