package dev.cerus.edina.bot.sandbox.runner;

import java.util.function.BiConsumer;

public class SandboxSubscription<R> {

    private boolean startComplete;
    private Runnable startCallback;
    private boolean endComplete;
    private R endResult;
    private Throwable endError;
    private BiConsumer<R, Throwable> endCallback;

    public SandboxSubscription<R> onStart(final Runnable callback) {
        this.startCallback = callback;
        if (this.startComplete) {
            callback.run();
        }
        return this;
    }

    public SandboxSubscription<R> onEnd(final BiConsumer<R, Throwable> callback) {
        this.endCallback = callback;
        if (this.endComplete) {
            callback.accept(this.endResult, this.endError);
        }
        return this;
    }

    void start() {
        this.startComplete = true;
        if (this.startCallback != null) {
            this.startCallback.run();
        }
    }

    void end(final R result, final Throwable error) {
        this.endResult = result;
        this.endError = error;
        this.endComplete = true;
        if (this.endCallback != null) {
            this.endCallback.accept(result, error);
        }
    }

}
