package lt.lb.commons.threads;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lt.lb.commons.F;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class RunnableDecorators {

    /**
     * Decorates Runnable to be interrupted after a set amount of time. If given
     * runnable ignores interrupts, then we can't do anything.
     *
     * @param time
     * @param run
     * @return
     */
    public static UnsafeRunnable withTimeout(WaitTime time, Runnable run) {
        return () -> {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            Thread toCancel = Thread.currentThread();
            AtomicBoolean isDone = new AtomicBoolean(false);
            Runnable interrupter = () -> {
                if (!isDone.get()) {
                    toCancel.interrupt();
                }else{
                    service.shutdown();
                }
            };
            service.schedule(interrupter, time.time, time.unit);
            Optional<Throwable> checkedRun = F.checkedRun(run);
            isDone.set(true);
            service.shutdownNow();
            if (checkedRun.isPresent()) {
                throw new ExecutionException(checkedRun.get());
            }

        };

    }
    
    /**
     *
     * Decorates Runnable to be interrupted after a set amount of time. If given
     * runnable ignores interrupts, then we can't do anything. Runnable then
     * tries to complete execution again with given timeout constraint. Repeat
     * amount is limited.
     *
     * @param time
     * @param repeatTimeIfTimeout repeat limit
     * @param run
     * @return
     */
    public static UnsafeRunnable withTimeoutRepeat(WaitTime time, Integer repeatTimeIfTimeout, Runnable run) {
        return withTimeoutRepeat(time, false, repeatTimeIfTimeout, run);
    }

    /**
     * Decorates Runnable to be interrupted after a set amount of time. If given
     * runnable ignores interrupts, then we can't do anything. Runnable then
     * tries to complete execution again with given timeout constraint. No
     * repeat limit.
     *
     * @param time
     * @param run
     * @return
     */
    public static UnsafeRunnable withTimeoutRepeatUntilDone(WaitTime time, Runnable run) {
        return withTimeoutRepeat(time, true, Integer.MAX_VALUE, run);
    }

    private static UnsafeRunnable withTimeoutRepeat(WaitTime time, boolean always, Integer repeatTimeIfTimeout, Runnable run) {
        return () -> {
            Integer repeat = repeatTimeIfTimeout;
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            Thread toCancel = Thread.currentThread();
            AtomicBoolean isDone = new AtomicBoolean(false);
            AtomicBoolean interruptReached = new AtomicBoolean(false);
            Runnable interrupter = () -> {
                if (!isDone.get()) {
                    interruptReached.set(true);
                    toCancel.interrupt();
                }
            };
            while ((always || repeat > 0) && !isDone.get()) {
                service.schedule(interrupter, time.time, time.unit);
                Optional<Throwable> map = F.checkedRun(run);
                if (map.isPresent()) {
                    if (interruptReached.get() && map.get() instanceof InterruptedException) {
                        //repeat
                        interruptReached.set(false);
                        if (!always) {
                            repeat--;
                        }
                    } else {
                        //some exception happened, report
                        isDone.set(true);
                        service.shutdownNow();
                        throw new ExecutionException(map.get());
                    }
                } else {
                    //all good
                    isDone.set(true);
                    service.shutdownNow();
                }

            }
            //if repeat amount is not positive, we never enter loop.
            service.shutdown();

        };
    }
}
