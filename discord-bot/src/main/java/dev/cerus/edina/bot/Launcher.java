package dev.cerus.edina.bot;

import dev.cerus.edina.bot.sandbox.provider.SandboxProvider;
import dev.cerus.edina.bot.sandbox.provider.SimpleSandboxProvider;
import dev.cerus.edina.bot.sandbox.runner.QueuedSandboxRunner;
import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;
import dev.cerus.edina.bot.settings.BotSettings;
import java.io.IOException;
import javax.security.auth.login.LoginException;

public class Launcher {

    public static void main(final String[] args) {
        // Load settings
        final BotSettings settings = new BotSettings();
        try {
            settings.load();
        } catch (final IOException e) {
            System.out.println("Failed to load settings");
            return;
        }

        // Create stuff
        final SandboxRunner runner = new QueuedSandboxRunner();
        final SandboxProvider provider = new SimpleSandboxProvider(runner, settings.getCommand());
        final EdinaBot bot = new EdinaBot(settings, runner, provider);
        try {
            bot.start();
        } catch (final LoginException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to start bot");
            return;
        }

        // Shutdown logic
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bot.close();
                runner.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
