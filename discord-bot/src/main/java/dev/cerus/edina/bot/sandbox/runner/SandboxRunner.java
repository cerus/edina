package dev.cerus.edina.bot.sandbox.runner;

public interface SandboxRunner extends AutoCloseable {

    <T> SandboxSubscription<T> submit(SandboxRunnable<T> runnable, Runnable cleanupAction);

    int queued();

    @FunctionalInterface
    interface SandboxRunnable<T> {

        T run() throws Throwable;

    }

}
