package dev.cerus.edina.bot.sandbox.provider;

import dev.cerus.edina.bot.sandbox.Sandbox;

/**
 * Creates sandboxes
 */
public interface SandboxProvider {

    /**
     * Create a sandbox
     *
     * @param code The code to run
     *
     * @return The new sandbox
     */
    Sandbox createSandbox(String code);

}
