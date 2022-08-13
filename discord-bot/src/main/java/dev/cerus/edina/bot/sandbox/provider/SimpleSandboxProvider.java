package dev.cerus.edina.bot.sandbox.provider;

import dev.cerus.edina.bot.sandbox.Sandbox;
import dev.cerus.edina.bot.sandbox.SandboxSettings;
import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;

public class SimpleSandboxProvider implements SandboxProvider {

    private final SandboxRunner runner;
    private final String edinaCommand;

    public SimpleSandboxProvider(final SandboxRunner runner, final String edinaCommand) {
        this.runner = runner;
        this.edinaCommand = edinaCommand;
    }

    @Override
    public Sandbox createSandbox(final String code) {
        return new Sandbox(
                this.createFreshSettings(),
                this.runner,
                code
        );
    }

    private SandboxSettings createFreshSettings() {
        final String fileName = "script_" + System.currentTimeMillis();
        return new SandboxSettings(
                fileName + ".edina",
                this.edinaCommand
                        .replace("%out%", fileName + ".jar")
                        .replace("%in%", fileName + ".edina")
        );
    }

}
