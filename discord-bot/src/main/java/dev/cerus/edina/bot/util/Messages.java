package dev.cerus.edina.bot.util;

import java.awt.Color;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * Utility for predefined messages
 */
public class Messages {

    // TODO: Don't hardcode stuff
    private static final String EMOTE_WAITING = ":hourglass_flowing_sand:";
    private static final String EMOTE_WAITING_RUNNER = ":zzz:";
    private static final String EMOTE_ERROR = "<:not_like_this:1008168864162525275>";
    private static final String EMOTE_TIMEOUT = "<:carlos:1008169977586651238>";
    private static final String EMOTE_SUCCESS = ":partying_face: ";

    private Messages() {
    }

    /**
     * "Waiting for runner" sandbox message
     *
     * @param initiator The initiating user
     *
     * @return A new message
     */
    public static Message sandboxEmpty(final User initiator) {
        return new MessageBuilder()
                .setEmbeds(initEmbed()
                        .setColor(Color.DARK_GRAY)
                        .setFooter(initiator.getAsTag(), initiator.getEffectiveAvatarUrl())
                        .setTitle(EMOTE_WAITING_RUNNER + "  Waiting for runner...")
                        .setDescription("```\n \n```")
                        .build())
                .build();
    }

    /**
     * "Playing" sandbox message
     *
     * @param initiator The initiating user
     *
     * @return A new message
     */
    public static Message sandboxWait(final User initiator) {
        return new MessageBuilder()
                .setEmbeds(initEmbed()
                        .setColor(Color.PINK)
                        .setFooter(initiator.getAsTag(), initiator.getEffectiveAvatarUrl())
                        .setTitle(EMOTE_WAITING + "  Playing...")
                        .setDescription("```\n \n```")
                        .build())
                .build();
    }

    /**
     * "Sandbox timed out" sandbox message
     *
     * @param initiator The initiating user
     *
     * @return A new message
     */
    public static Message sandboxTimeout(final User initiator, final String data) {
        return new MessageBuilder()
                .setEmbeds(initEmbed()
                        .setColor(Color.ORANGE)
                        .setFooter(initiator.getAsTag(), initiator.getEffectiveAvatarUrl())
                        .setTitle(EMOTE_TIMEOUT + "  Sandbox timed out")
                        .setDescription("```\n%s```".formatted(data))
                        .build())
                .build();
    }

    /**
     * "Sandbox completed with error" sandbox message
     *
     * @param initiator The initiating user
     *
     * @return A new message
     */
    public static Message sandboxError(final User initiator, final String data) {
        return new MessageBuilder()
                .setEmbeds(initEmbed()
                        .setColor(Color.RED)
                        .setFooter(initiator.getAsTag(), initiator.getEffectiveAvatarUrl())
                        .setTitle(EMOTE_ERROR + "  Sandbox completed with error")
                        .setDescription("```\n%s```".formatted(data))
                        .build())
                .build();
    }

    /**
     * "Sandbox completed successfully" sandbox message
     *
     * @param initiator The initiating user
     *
     * @return A new message
     */
    public static Message sandboxSuccess(final User initiator, final String data) {
        return new MessageBuilder()
                .setEmbeds(initEmbed()
                        .setColor(Color.GREEN)
                        .setFooter(initiator.getAsTag(), initiator.getEffectiveAvatarUrl())
                        .setTitle(EMOTE_SUCCESS + "  Sandbox completed successfully")
                        .setDescription("```\n%s```".formatted(data))
                        .build())
                .build();
    }

    private static EmbedBuilder initEmbed() {
        return new EmbedBuilder()
                .setAuthor(
                        "Edina language bot",
                        "https://github.com/cerus/edina",
                        "https://cerus.dev/img/edina_lang_logo.png"
                )
                .setTimestamp(Instant.now());
    }

}
