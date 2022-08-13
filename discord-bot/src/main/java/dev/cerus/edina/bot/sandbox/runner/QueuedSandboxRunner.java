package dev.cerus.edina.bot.sandbox.runner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueuedSandboxRunner implements SandboxRunner {

    private final ExecutorService executorService = new ThreadPoolExecutor(
            1,
            1,
            5000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    );

    @Override
    public <T> CompletableFuture<T> submit(final SandboxRunnable<T> runnable, final Runnable cleanupAction) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            Throwable error = null;
            T result = null;
            try {
                result = runnable.run();
            } catch (final Throwable err) {
                error = err;
            }
            try {
                cleanupAction.run();
            } catch (final Throwable ignored) {
            }
            if (error != null) {
                future.completeExceptionally(error);
            } else {
                future.complete(result);
            }
        });
        return future;
    }

    @Override
    public void close() throws Exception {
        this.executorService.shutdownNow();
    }

}
