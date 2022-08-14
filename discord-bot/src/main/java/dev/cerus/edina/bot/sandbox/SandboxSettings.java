package dev.cerus.edina.bot.sandbox;

/**
 * Sandbox specific settings
 *
 * @param fileName The name of the script file
 * @param command  The command to run
 */
public record SandboxSettings(String fileName, String command) {

    /**
     * The name of the jar file
     *
     * @return Jar name
     */
    String jarName() {
        return this.fileName.substring(0, this.fileName.lastIndexOf('.')) + ".jar";
    }

}
