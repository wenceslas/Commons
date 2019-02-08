package lt.lb.commons.threads;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lt.lb.commons.F;

/**
 *
 * Spawns new threads on demand. If all tasks are exhausted, thread terminates
 * immediately.
 *
 * @author laim0nas100
 */
public class FastExecutor implements CloseableExecutor {

    protected Collection<Runnable> tasks = new ConcurrentLinkedDeque<>();
    protected ThreadGroup tg = new ThreadGroup("FastExecutor");

    protected volatile boolean open = true;
    protected int maxThreads; // 
    protected AtomicInteger startingThreads = new AtomicInteger(0);
    protected AtomicInteger runningThreads = new AtomicInteger(0);
    protected AtomicInteger finishingThreads = new AtomicInteger(0);

    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    public FastExecutor(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setErrorChannel(Consumer<Throwable> channel) {
        errorChannel = channel;
    }

    public Consumer<Throwable> getErrorChannel() {
        return errorChannel;
    }

    @Override
    public void execute(Runnable command) {
        if (!open) {
            throw new IllegalStateException("Not open");
        }
        if (command == null) {
            throw new NullPointerException("null runnable recieved");
        }
        Deque<Runnable> cast = F.cast(tasks);
        cast.addFirst(command);
        update(this.maxThreads);
    }

    protected Runnable getMainBody() {
        return () -> {
            Deque<Runnable> cast = F.cast(tasks);
            while (!cast.isEmpty()) {
                Runnable last = cast.pollLast();
                if (last != null) {
                    last.run();
                }

            }
        };
    }

    protected final Runnable getRun(final int bakedMax) {
        return () -> {
            runningThreads.incrementAndGet();
            startingThreads.decrementAndGet(); //thread started

            try {
                getMainBody().run();
            } catch (Throwable th) {
                try {
                    getErrorChannel().accept(th);
                } catch (Throwable err) {// we really screwed now
                    err.printStackTrace();
                }
            } finally {
                finishingThreads.incrementAndGet(); // thread is finishing
                runningThreads.decrementAndGet(); //thread no longer running (not really)

                update(bakedMax);
                finishingThreads.decrementAndGet(); // thread end finishing
            }
        };
    }

    protected void update(int maxT) {
        if (tasks.isEmpty()) {
            return;
        }
        this.maybeStartThread(maxT);

    }

    protected Thread startThread(final int maxT) {
        Thread t = new Thread(tg, getRun(maxT));
        t.setName("Fast Executor " + t.getName());
        t.start();
        return t;
    }

    protected void maybeStartThread(final int maxT) {

        if (maxT == 0) {
            startingThreads.incrementAndGet();//because run decrements
            getRun(maxT).run();
        } else if (maxT < 0) {//unlimited threads
            startingThreads.incrementAndGet();//because run decrements
            startThread(maxT);
        } else { // limitedThreads
            final int starting = startingThreads.incrementAndGet();
            if (starting > maxT) { // we dispatch threads too often
                this.startingThreads.decrementAndGet();
            } else if (starting + runningThreads.get() <= maxT) { // we are within limit
                startThread(maxT);
            } else { // don't start new thread, just update value
                this.startingThreads.decrementAndGet();
            }
        }
    }

    /**
     * Threads close automatically when all tasks are exhausted. This method
     * ensures no more runnables gets submitted. Does not actually wait for
     * threads to close.
     */
    @Override
    public void close() {
        if (!this.open) {
            throw new IllegalStateException("Is not open");
        }
        this.open = false;

    }
}
