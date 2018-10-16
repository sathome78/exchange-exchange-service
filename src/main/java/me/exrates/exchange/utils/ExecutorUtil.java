package me.exrates.exchange.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class ExecutorUtil {

    public static ExecutorService getExecutorService(int threads) {
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("listenerPool -%d")
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .build();
        return Executors.newFixedThreadPool(threads, factory);
    }

    public static void shutdownExecutor(ExecutorService executor) {
        try {
            log.debug("Attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.warn("Tasks interrupted", ex);
        } finally {
            if (!executor.isTerminated()) {
                log.warn("Cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.debug("Executor shutdown finished");
        }
    }

    public static void shutdownExecutor(ExecutorService executor, int timeoutInSeconds) {
        try {
            log.debug("Attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.warn("Tasks interrupted", ex);
        } finally {
            if (!executor.isTerminated()) {
                log.warn("Cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.debug("Executor shutdown finished");
        }
    }
}
