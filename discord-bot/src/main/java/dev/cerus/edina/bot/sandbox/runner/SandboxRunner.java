package dev.cerus.edina.bot.sandbox.runner;

import java.util.concurrent.CompletableFuture;

public interface SandboxRunner extends AutoCloseable {

    <T> CompletableFuture<T> submit(SandboxRunnable<T> runnable, Runnable cleanupAction);

    @FunctionalInterface
    interface SandboxRunnable<T> {

        T run() throws Throwable;

    }

}