package dev.cerus.edina.bot;

import dev.cerus.edina.bot.listener.PlayListener;
import dev.cerus.edina.bot.sandbox.provider.SandboxProvider;
import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;
import dev.cerus.edina.bot.settings.BotSettings;
import dev.cerus.edina.bot.task.PresenceUpdateTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class EdinaBot implements AutoCloseable {

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final BotSettings botSettings;
    private final SandboxRunner runner;
    private final SandboxProvider provider;
    private JDA jda;

    public EdinaBot(final BotSettings botSettings, final SandboxRunner runner, final SandboxProvider provider) {
        this.botSettings = botSettings;
        this.runner = runner;
        this.provider = provider;
    }

    public void start() throws LoginException, InterruptedException {
        this.jda = JDABuilder.create(this.botSettings.getToken(),
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new PlayListener(this.provider)).build().awaitReady();
        this.exec.scheduleAtFixedRate(new PresenceUpdateTask(this.jda, this.runner), 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws Exception {
        this.exec.shutdown();
        this.jda.shutdown();
    }

}
