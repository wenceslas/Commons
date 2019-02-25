package lt.lb.commons.iteration;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseROI<T> implements ReadOnlyIterator<T>{

    protected Integer index = -1;
    protected T current;
    
    @Override
    public Integer getCurrentIndex() {
        return index;
    }

    @Override
    public T getCurrent() {
        return current;
    }
    
    protected T setCurrent(T val){
        current = val;
        return current;
    }
    
    
}