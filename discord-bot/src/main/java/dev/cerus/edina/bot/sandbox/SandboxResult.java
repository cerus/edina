package dev.cerus.edina.bot.sandbox;

public record SandboxResult(Type type, String data) {

    public static SandboxResult of(final Type type, final String data) {
        return new SandboxResult(type, data);
    }

    public enum Type {
        TIMEOUT,
        ERROR,
        SUCCESS
    }
}
