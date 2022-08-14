package dev.cerus.edina.bot.sandbox.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class QueuedSandboxRunner implements SandboxRunner {

    private final Map<Integer, Runnable> sandboxStartMap = new HashMap<>();
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            1,
            1,
            5000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    ) {
        @Override
        public void execute(@NotNull final Runnable command) {
            System.out.println("[Runner] Submitting sandbox #" + command.hashCode());
            super.execute(command);
        }

        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            System.out.println("[Runner] Playing with sandbox #" + r.hashCode());
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);
            System.out.println("[Runner] Destroying sandbox #" + r.hashCode());
            QueuedSandboxRunner.this.sandboxStartMap.remove(r.hashCode());
        }
    };

    @Override
    public <T> SandboxSubscription<T> submit(final SandboxRunnable<T> runnable, final Runnable cleanupAction) {
        final SandboxSubscription<T> subscription = new SandboxSubscription<>();
        this.executorService.submit(() -> {
            subscription.start();

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
            subscription.end(result, error);
        });
        return subscription;
    }

    @Override
    public int queued() {
        return this.executorService.getQueue().size();
    }

    @Override
    public void close() throws Exception {
        this.executorService.shutdownNow();
    }

}
