package dev.cerus.edina.bot;

import dev.cerus.edina.bot.settings.BotSettings;
import java.io.IOException;

public class Launcher {

    public static void main(final String[] args) {
        final BotSettings settings = new BotSettings();
        try {
            settings.load();
        } catch (final IOException e) {
            System.out.println("Failed to load settings");
            return;
        }
    }

}
