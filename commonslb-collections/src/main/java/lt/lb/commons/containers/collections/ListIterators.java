package lt.lb.commons.containers.collections;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public abstract class ListIterators {

    public static <T, R> ListIterator<R> map(ListIterator<T> original, Function<? super T, ? extends R> func, Function<? super R, ? extends T> func2) {
        return new ListIterator<R>() {
            @Override
            public void add(R e) {
                original.add(func2.apply(e));
            }

            @Override
            public void set(R e) {
                original.set(func2.apply(e));
            }

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return original.hasPrevious();
            }

            @Override
            public R next() {
                return func.apply(original.next());
            }

            @Override
            public int nextIndex() {
                return original.nextIndex();
            }

            @Override
            public R previous() {
                return func.apply(original.previous());
            }

            @Override
            public int previousIndex() {
                return original.previousIndex();
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }

    public static final class SkippingListIterator<T> implements ListIterator<T> {

        protected boolean nextCalled = false;
        protected int cursor = -1;
        protected boolean directionForward;
        protected Predicate<T> notNull;
        protected List<T> list;

        public SkippingListIterator(int i, Predicate<T> nullCheck, List<T> list) {
            this.list = list;
            this.notNull = nullCheck.negate();
            if (i > 0) {
                while (i > cursor) {
                    next();
                }
            }
        }

        public SkippingListIterator(int i, List<T> list) {
            this(i, (T t) -> false, list);
        }

        @Override
        public boolean hasNext() {
            return inRange(this.nextIndex());
        }

        @Override
        public T next() {
            this.directionForward = true;
            cursor = this.find(1, cursor + 1);
            return checkedGet(cursor);

        }

        public T checkedGet(int i) {
            if (inRange(i)) {
                return list.get(i);
            } else {
                throw new NoSuchElementException("Index:" + i + " size:" + list.size());
            }
        }

        @Override
        public T previous() {
            T get = checkedGet(find(-1, cursor));
            this.directionForward = false;
            cursor = this.find(-1, cursor - 1);
            return get;
        }

        public int find(int inc, int i) {
            if (inc < 0 && i >= list.size()) {
                i = list.size() - 1;
            } else if (inc > 0 && i < 0) {
                i = 0;
            } else if (inc == 0) {
                throw new IllegalArgumentException();
            }

            for (; i < list.size() && i >= 0; i += inc) {
                T get = this.checkedGet(i);
                if (notNull.test(get)) {
                    return i;
                }
            }
            return -1;

        }

        protected boolean inRange(int i) {
            return i >= 0 && i < list.size();
        }

        @Override
        public boolean hasPrevious() {
            return inRange(this.previousIndex());
        }

        @Override
        public int nextIndex() {
            int find = this.find(1, cursor + 1);
            if (inRange(find)) {
                return find;
            } else {
                return list.size();
            }
        }

        @Override
        public int previousIndex() {
            return this.find(-1, cursor);
        }

        @Override
        public void remove() {
            if (inRange(this.cursor)) {
                list.remove(this.cursor);
                if (!inRange(this.cursor)) {
                    cursor = this.find(-1, cursor);
                }
            } else {
                throw new IllegalStateException();
            }

        }

        @Override
        public void set(T e) {
            int i = this.cursor;
            if (!this.directionForward) {
                i = this.nextIndex();
            }
            if (inRange(i)) {
                list.set(i, e);
            } else {
                throw new IllegalStateException();
            }

        }

        @Override
        public void add(T e) {
            if (inRange(this.nextIndex())) {
                list.add(this.nextIndex(), e);
            } else {
                throw new IllegalStateException();
            }
        }

    }

}