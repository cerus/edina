package dev.cerus.edina.bot.task;

import dev.cerus.edina.bot.sandbox.runner.SandboxRunner;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

public class PresenceUpdateTask implements Runnable {

    private final JDA jda;
    private final SandboxRunner runner;
    private int last = -1;

    public PresenceUpdateTask(final JDA jda, final SandboxRunner runner) {
        this.jda = jda;
        this.runner = runner;
    }

    @Override
    public void run() {
        final int queued = this.runner.queued();
        if (queued == this.last) {
            return;
        }
        this.last = queued;
        this.jda.getPresence().setPresence(Activity.watching(queued + " sandboxes"), false);
    }
}
