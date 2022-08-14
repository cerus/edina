package dev.cerus.edina.bot.sandbox;

import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;
import dev.cerus.edina.bot.sandbox.runner.SandboxSubscription;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Represents a sandbox
 */
public class Sandbox {

    private final SandboxSettings settings;
    private final SandboxRunner runner;
    private final String code;

    public Sandbox(final SandboxSettings settings, final SandboxRunner runner, final String code) {
        this.settings = settings;
        this.runner = runner;
        this.code = code;
    }

    /**
     * Play with this sandbox
     *
     * @return A subscription containing the result
     */
    public SandboxSubscription<SandboxResult> play() {
        return this.runner.submit(() -> {
            // Create files
            final File scriptFile = new File(this.settings.fileName());
            final File logFile = new File(this.settings.fileName() + ".log");
            try (final FileOutputStream out = new FileOutputStream(scriptFile)) {
                out.write(this.code.getBytes(StandardCharsets.UTF_8));
            }

            // Start process
            final ProcessBuilder builder = new ProcessBuilder(this.settings
                    .command().split("\\s+"))
                    .redirectOutput(logFile);
            builder.environment().put("TERM", "dumb");
            final Process edinaProc = builder.start();
            final boolean terminated = edinaProc.waitFor(5, TimeUnit.SECONDS);

            if (!terminated) {
                // Process is taking more than 5 seconds to complete, kill it
                edinaProc.destroyForcibly();
                return SandboxResult.of(SandboxResult.Type.TIMEOUT, "Sandbox timeout");
            } else {
                // Process has completed
                final int exit = edinaProc.exitValue();
                final String data = Files.readString(logFile.toPath());

                if (exit == 0) {
                    // No error
                    return SandboxResult.of(SandboxResult.Type.SUCCESS, data);
                } else {
                    // Error
                    return SandboxResult.of(SandboxResult.Type.ERROR, data);
                }
            }
        }, () -> {
            // Clean up the generated files
            final File scriptFile = new File(this.settings.fileName());
            final File jarFile = new File(this.settings.jarName());
            final File logFile = new File(this.settings.fileName() + ".log");
            scriptFile.delete();
            jarFile.delete();
            logFile.delete();
        });
    }

}
