package dev.cerus.edina.edinaj.asm;

import java.io.IOException;

/**
 * Launcher template
 * <p>
 * Simple program launcher
 */
public class Launcher {

    public static void main(final String[] args) {
        final Stack stack = new Stack();
        final Natives natives = new Natives(stack);
        natives.setRestricted(false);
        final App app = new App(stack, natives);
        app.run();
        try {
            natives.closeAll();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
