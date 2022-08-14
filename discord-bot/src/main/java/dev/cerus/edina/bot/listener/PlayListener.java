package dev.cerus.edina.bot.listener;

import dev.cerus.edina.bot.sandbox.Sandbox;
import dev.cerus.edina.bot.sandbox.provider.SandboxProvider;
import dev.cerus.edina.bot.util.Messages;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PlayListener extends ListenerAdapter {

    private final Set<Long> cooldown = new HashSet<>();
    private final SandboxProvider sandboxProvider;

    public PlayListener(final SandboxProvider sandboxProvider) {
        this.sandboxProvider = sandboxProvider;
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        final Message msg = event.getMessage();
        if (!msg.getMentions().isMentioned(event.getJDA().getSelfUser(), Message.MentionType.USER)) {
            return;
        }
        final String msgContent = msg.getContentRaw().replace(event.getJDA().getSelfUser().getAsMention(), "").trim();
        if (!msgContent.replace("\n", "").matches("```.+```")) {
            return;
        }
        if (this.cooldown.contains(msg.getAuthor().getIdLong())) {
            return;
        }
        final String code = msgContent.substring(msgContent.indexOf('\n'), msgContent.length() - 3).trim();

        final User initiator = msg.getAuthor();
        final Message replyMsg = Messages.sandboxEmpty(initiator);
        this.cooldown.add(msg.getAuthor().getIdLong());
        msg.reply(replyMsg).queue(message -> {
            final Sandbox sandbox = this.sandboxProvider.createSandbox(code);
            sandbox.play().whenComplete((future, t) -> {
                message.editMessage(Messages.sandboxWait(initiator)).queue();
                future.whenComplete((sandboxResult, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Hard error");
                        throwable.printStackTrace();

                        final ByteArrayOutputStream errorOut = new ByteArrayOutputStream();
                        final PrintWriter writer = new PrintWriter(errorOut);
                        throwable.printStackTrace(writer);
                        final String capturedError = errorOut.toString();
                        message.editMessage(Messages.sandboxError(initiator, capturedError)).queue();
                    } else {
                        final String data = this.stripData(sandboxResult.data());
                        switch (sandboxResult.type()) {
                            case TIMEOUT -> message.editMessage(Messages.sandboxTimeout(initiator, data)).queue();
                            case ERROR -> message.editMessage(Messages.sandboxError(initiator, data)).queue();
                            case SUCCESS -> message.editMessage(Messages.sandboxSuccess(initiator, data)).queue();
                        }
                    }
                    this.cooldown.remove(msg.getAuthor().getIdLong());
                });
            });
        });
    }

    private String stripData(final String data) {
        String result = data;
        if (result.length() > 500) {
            result = "..." + result.substring(result.length() - 500);
        }
        if (result.split("\n").length > 16) {
            final List<String> lines = new ArrayList<>(Arrays.asList(result.split("\n")));
            while (lines.size() > 16) {
                lines.remove(0);
            }
            result = "...\n" + String.join("\n", lines);
        }
        if (result.length() == 0) {
            result = " ";
        }
        return result;
    }

}
