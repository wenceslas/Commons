package lt.lb.commons;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
@Deprecated
public class Tracer {

    private Predicate disabled = (s) -> false;

    private Tracer(String s) {
        this.cls = s;
    }

    public Tracer setDisabled(Predicate pr) {
        disabled = pr;
        return this;
    }

    private String cls;

    public static Tracer get(String c) {
        Tracer trace = new Tracer(c);
        return trace;
    }

    public static Tracer get(Class c) {
        return get(c.getName());
    }

    public static PrintStream stream = System.out;
    public static Executor service = Executors.newSingleThreadExecutor();
    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public void print(final String str) {
        if (disabled.test(str)) {
            return;
        }
        final Thread t = Thread.currentThread();
        final long millis = System.currentTimeMillis();

        Callable<String> call = () -> str;
        submit(t, millis, cls, call);
    }

    private static void submit(final Thread t, final long millis, final String cls, final Callable<String> str) {
        Runnable run = () -> {
            String strTime = Instant.ofEpochMilli(millis).atZone(ZoneOffset.systemDefault()).format(format);
            String calledString = "";
            try {
                calledString = str.call();
            } catch (Exception ex) {
                calledString = ex.getLocalizedMessage();
            }
            String s = strTime + " " + cls + " " + t.getName() + " " + calledString;
            stream.println(s);
        };
        service.execute(run);
    }

    public void dump(final Object o) {
        if (this.disabled.test(o)) {
            return;
        }
        final Thread t = Thread.currentThread();
        final long millis = System.currentTimeMillis();
        Callable<String> call = () -> ReflectionUtils.reflectionString(o, 5);
        submit(t, millis, cls, call);
    }

}
