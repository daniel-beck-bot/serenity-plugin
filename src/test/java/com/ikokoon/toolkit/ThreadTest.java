package com.ikokoon.toolkit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * This test if for the thread utilities, which has methods to wait for threads etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-03-2011
 */
public class ThreadTest {

    // The runnable to sleep for a while
    class Sleepy implements Runnable {

        long sleep;

        public Sleepy() {
            this(1000);
        }

        public Sleepy(long sleep) {
            this.sleep = sleep;
        }

        public void run() {
            Thread.sleep(sleep);
        }
    }

    // The class to destroy the executor pool
    class Destroyer implements Runnable {
        public void run() {
            Thread.sleep(1000);
            Thread.destroy();
        }
    }

    private Logger logger;

    @Before
    public void before() {
        logger = LoggerFactory.getLogger(this.getClass());
        Thread.initialize();
    }

    @After
    public void after() {
        Thread.destroy();
    }

    @Test
    public void waitForThreads() {
        List<java.lang.Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            java.lang.Thread thread = new java.lang.Thread(new Sleepy());
            thread.start();
            threads.add(thread);
        }
        Thread.waitForThreads(threads);
        // Verify that all the threads are dead
        for (final java.lang.Thread thread : threads) {
            assertFalse("All the threads should have died : ", thread.isAlive());
        }
        assertTrue("We just want to exit here after the threads die : ", true);
    }

    @Test
    public void waitForFuture() {
        // We just wait for this future to finish,
        // must be less than the time we expect to wait
        long start = System.currentTimeMillis();
        Future<?> future = Thread.submit(null, new Sleepy(3000));
        Thread.waitForFuture(future, Integer.MAX_VALUE);
        assertTrue(System.currentTimeMillis() - start < 4000);

        // We destroy this future and return from the wait method
        start = System.currentTimeMillis();
        future = Thread.submit(null, new Sleepy(Integer.MAX_VALUE));
        logger.info("Going into wait before destroying the thread pool : " + future);
        new java.lang.Thread(new Destroyer()).start();
        Thread.waitForFuture(future, Integer.MAX_VALUE);
        long duration = System.currentTimeMillis() - start;
        logger.info("Duration : " + duration);
        assertTrue(duration < 20000);
    }

    @Test
    public void submitDestroy() {
        if (!Thread.isInitialized()) {
            Thread.initialize();
        }
        String name = Long.toHexString(System.currentTimeMillis());
        Runnable sleepy = new Sleepy(Integer.MAX_VALUE);
        Future<?> future = Thread.submit(name, sleepy);
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());
        Thread.destroy(name);
        logger.info("Future : " + future.isCancelled() + ", " + future.isDone());

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
    }

    /**
     * This method just checks the concurrency of the
     * threading, that there are no blocking/deadlocking synchronized blocks.
     */
    @Test
    public void multiThreaded() {
        final int iterations = 100;
        List<java.lang.Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            java.lang.Thread thread = new java.lang.Thread(new Runnable() {
                public void run() {
                    int i = iterations;
                    do {
                        Thread.sleep(10);
                        Thread.submit(this.toString(), new Sleepy());
                        Thread.getFutures(this.toString());
                        Thread.submit(null, new Sleepy());
                        Thread.destroy(this.toString());
                    } while (i-- > 0);
                }
            });
            thread.start();
            threads.add(thread);
        }
        Thread.waitForThreads(threads);
        // If it dead locks we will never get here
    }

    @Test
    public void cancelForkJoinPool() {
        ForkJoinPool forkJoinPool = Thread.getForkJoinPool(this.getClass().getSimpleName(), 3);
        ForkJoinPool cancelledForkJoinPool = Thread.cancelForkJoinPool(this.getClass().getSimpleName());
        assertEquals(forkJoinPool, cancelledForkJoinPool);
        assertTrue(cancelledForkJoinPool.isShutdown());
        assertTrue(cancelledForkJoinPool.isTerminated());
    }

    @Test
    public void cancelAllForkJoinPools() {
        ForkJoinPool forkJoinPool = Thread.getForkJoinPool(this.getClass().getSimpleName(), 3);
        Thread.cancelAllForkJoinPools();
        assertTrue(forkJoinPool.isShutdown());
        assertTrue(forkJoinPool.isTerminated());
    }

    @Test
    public void executeForkJoinTasks() {
        final String forkJoinPoolName = this.getClass().getSimpleName();
        ForkJoinTask<Object> forkJoinTask = new RecursiveTask<Object>() {
            @Override
            protected Object compute() {
                Thread.sleep(5000);
                return null;
            }
        };
        new java.lang.Thread(new Runnable() {
            public void run() {
                Thread.sleep(3000);
                Thread.cancelForkJoinPool(forkJoinPoolName);
            }
        }).start();
        try {
            Thread.executeForkJoinTasks(forkJoinPoolName, 3, forkJoinTask);
            Thread.sleep(10000);
        } catch (CancellationException e) {
            // Ignore?
        }
        assertTrue(forkJoinTask.isCancelled());
    }

}