package lt.lb.commons.jobs;

/**
 *
 * @author laim0nas100
 */
public final class SystemJobEvent<T> extends JobEvent<T> {

    public final SystemJobEventName enumName;

    public SystemJobEvent(SystemJobEventName event, Job source) {
        super(event.eventName, source);
        enumName = event;
    }

    public SystemJobEvent(SystemJobEventName event, Job source, T data) {
        super(event.eventName, source, data);
        enumName = event;
    }

}
