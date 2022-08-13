package dev.cerus.edina.bot.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BotSettings {

    private static final File FILE = new File("settings.json");

    private String token;
    private String command;

    public void load() throws IOException {
        if (!FILE.exists()) {
            this.token = "<token>";
            this.command = "<cmd>";
            this.save();
        }
        try (final FileInputStream inputStream = new FileInputStream(FILE);
             final InputStreamReader reader = new InputStreamReader(inputStream)) {
            final JsonObject settingsObj = JsonParser.parseReader(reader).getAsJsonObject();
            this.token = settingsObj.get("token").getAsString();
            this.command = settingsObj.get("command").getAsString();
        }
    }

    public void save() throws IOException {
        try (final FileOutputStream out = new FileOutputStream(FILE)) {
            out.write(("""
                    {
                      "token": "%s",
                      "command": "%s"
                    }
                    """)
                    .formatted(
                            this.token,
                            this.command
                    ).getBytes(StandardCharsets.UTF_8));
        }
    }

    public String getToken() {
        return this.token;
    }

    public String getCommand() {
        return this.command;
    }

}
