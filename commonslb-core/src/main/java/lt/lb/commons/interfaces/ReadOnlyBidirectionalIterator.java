package lt.lb.commons.interfaces;

/**
 *
 * @author laim0nas100
 */
public interface ReadOnlyBidirectionalIterator<T> extends ReadOnlyIterator<T> {
    
    public boolean hasPrevious();
    
    public T previous();
    
}
