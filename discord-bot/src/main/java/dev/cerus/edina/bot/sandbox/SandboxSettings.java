package dev.cerus.edina.bot.sandbox;

public record SandboxSettings(String fileName, String command) {

    String jarName() {
        return this.fileName.substring(0, this.fileName.lastIndexOf('.')) + ".jar";
    }

}
