package dev.cerus.edina.bot.sandbox.runner;

/**
 * A runner that runs sandbox commands
 */
public interface SandboxRunner extends AutoCloseable {

    /**
     * Submit a sandbox command
     *
     * @param runnable      The command to run
     * @param cleanupAction The cleanup action
     * @param <T>           The command data
     *
     * @return A subscription
     */
    <T> SandboxSubscription<T> submit(SandboxRunnable<T> runnable, Runnable cleanupAction);

    /**
     * Amount of queued commands
     *
     * @return Queued commands
     */
    int queued();

    /**
     * Amount of running commands
     *
     * @return Running commands
     */
    int running();

    @FunctionalInterface
    interface SandboxRunnable<T> {

        T run() throws Throwable;

    }

}
