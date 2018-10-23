/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.F;
import lt.lb.commons.containers.Value;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class TaskBatcher implements Executor {
    
    private LinkedBlockingDeque<Future> deque = new LinkedBlockingDeque<>();
    private Executor exe;
    
    public TaskBatcher(Executor exe) {
        this.exe = exe;
    }
    
    public <T> Future<T> submit(Callable<T> call) {
        FutureTask<T> task = new FutureTask<>(call);
        execute(task);
        return task;
    }
    
    @Override
    public void execute(Runnable command) {
        FutureTask task;
        if (!(command instanceof FutureTask)) {
            task = new FutureTask(Executors.callable(command, null));
        } else {
            task = F.cast(command);
        }
        exe.execute(command);
        deque.addFirst(task);
    }
    
    public static class BatchRunSummary {
        
        public final int total;
        public final int timedOut;
        public final int successful;
        public final Collection<Throwable> failures;
        
        public BatchRunSummary(int total, int ok, int timedOut, Collection<Throwable> th) {
            this.total = total;
            this.successful = ok;
            this.timedOut = timedOut;
            this.failures = th;
            
        }
    }

    /**
     * Await. Stop on first failure.
     *
     * @return
     */
    public BatchRunSummary awaitFailOnFirst() {
        Value<BatchRunSummary> v = new Value<>();
        F.unsafeRun(() -> {  // 
            v.set(await(false, WaitTime.ofDays(0), WaitTime.ofDays(0))); // s should not throw InterruptedException
        });
        return v.get();
    }

    /**
     * Await. Don't stop on failure.
     *
     * @return
     */
    public BatchRunSummary awaitTolerateFails() {
        Value<BatchRunSummary> v = new Value<>();
        F.unsafeRun(() -> {  // 
            v.set(await(true, WaitTime.ofDays(0), WaitTime.ofDays(0))); // should not throw InterruptedException
        });
        return v.get();
    }

    /**
     * Manual await configuration, only 1 thread should use this at a time, so
     * it's synchronized.
     *
     * @param failFast cancel execution on first failure
     * @param pollWait how long to wait for new task to arrive. Set time to 0 or less to disable;
     * @param executionWait how long to wait for current task to end. Set time to 0 or less to disable.
     * @return
     */
    public synchronized BatchRunSummary await(boolean failFast, WaitTime pollWait, WaitTime executionWait){
        int total = 0;
        int ok = 0;
        int timeout = 0;
        ArrayDeque<Throwable> failures = new ArrayDeque<>();
        boolean waitPoll = pollWait.time > 0;
        boolean waitGet = executionWait.time > 0;
        while (!deque.isEmpty()) {
            Value<Future> last = new Value<>(null);
            F.checkedRun(()->{
                Future pollLast = waitPoll ? deque.pollLast(pollWait.time, pollWait.unit) : deque.pollLast();
                last.set(pollLast);
            });
            
            boolean failed = false;
            boolean timeEx = false;
            Throwable err = null;
            if (last.get() != null) {
                Future fut = last.get();
                if (waitGet) {
                    Optional<Throwable> ex = F.checkedRun(() -> {
                        fut.get(executionWait.time, executionWait.unit);
                    });
                    if (ex.isPresent()) {
                        Throwable get = ex.get();
                        if (get instanceof TimeoutException) {
                            timeEx = true;
                        } else {
                            failed = true;
                        }
                        err = get;
                    }
                } else {
                    Optional<Throwable> checkedRun = F.checkedRun(fut::get);
                    if (checkedRun.isPresent()) {
                        failed = true;
                        err = checkedRun.get();
                    }
                }
                total++;
                ok += failed ? 0 : 1;
                timeout += timeEx ? 1 : 0;
                boolean hasError = failed || timeEx;
                if (hasError) {
                    failures.add(err);
                }
                
                if (failFast && hasError) {
                    break;
                }
            }
        }
        
        return new BatchRunSummary(total, ok, timeout, failures);
    }
    
}
