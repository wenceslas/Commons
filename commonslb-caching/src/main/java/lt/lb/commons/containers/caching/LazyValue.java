package lt.lb.commons.containers.caching;

import java.util.function.Supplier;
import lt.lb.commons.Java;
import lt.lb.commons.containers.values.Value;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {

    protected Long loaded = null;
    protected Supplier<Boolean> loader = () -> loaded != null;
    protected Supplier<T> supply;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
    }

    public LazyValue(T value) {
        this(() -> value);
    }

    /**
     * Set value explicitly, update time
     *
     * @param val
     */
    @Override
    public void set(T val) {
        super.set(val);
        loaded = Java.getNanoTime();
    }

    /**
     * Get computed value or init computation and wait for it
     *
     * @return
     */
    @Override
    public T get() {
        if (!loader.get()) {
            return syncGet();
        }

        return super.get();
    }
    
    private synchronized T syncGet(){
        if (!loader.get()) {
            return super.setAndGet(supply);
        }

        return super.get();
    }

    /**
     * Invalidates value (needs recomputing)
     */
    public void invalidate() {
        loaded = null;
    }

}
