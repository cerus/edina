package dev.cerus.edina.bot.sandbox;

import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Sandbox {

    private final SandboxSettings settings;
    private final SandboxRunner runner;
    private final String code;

    public Sandbox(final SandboxSettings settings, final SandboxRunner runner, final String code) {
        this.settings = settings;
        this.runner = runner;
        this.code = code;
    }

    public CompletableFuture<SandboxResult> play() {
        return this.runner.submit(() -> {
            final File scriptFile = new File(this.settings.fileName());
            final File logFile = new File(this.settings.fileName() + ".log");
            try (final FileOutputStream out = new FileOutputStream(scriptFile)) {
                out.write(this.code.getBytes(StandardCharsets.UTF_8));
            }

            final ProcessBuilder builder = new ProcessBuilder(this.settings
                    .command().split("\\s+"))
                    .redirectOutput(logFile);
            builder.environment().put("TERM", "dumb");
            final Process edinaProc = builder.start();
            final boolean terminated = edinaProc.waitFor(5, TimeUnit.SECONDS);
            if (!terminated) {
                edinaProc.destroyForcibly();
                return SandboxResult.of(SandboxResult.Type.TIMEOUT, "Sandbox timeout");
            } else {
                final int exit = edinaProc.exitValue();
                final String data = Files.readString(logFile.toPath());

                if (exit == 0) {
                    return SandboxResult.of(SandboxResult.Type.SUCCESS, data);
                } else {
                    return SandboxResult.of(SandboxResult.Type.ERROR, data);
                }
            }
        }, () -> {
            final File scriptFile = new File(this.settings.fileName());
            final File jarFile = new File(this.settings.jarName());
            final File logFile = new File(this.settings.fileName() + ".log");
            scriptFile.delete();
            jarFile.delete();
            logFile.delete();
        });
    }

}
