package dev.cerus.edina.bot.sandbox.provider;

import dev.cerus.edina.bot.sandbox.Sandbox;

public interface SandboxProvider {

    Sandbox createSandbox(String code);

}
