package dev.cerus.edina.eddoc.util;

import dev.cerus.edina.eddoc.Launcher;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppPropertiesUtil {

    private static final String PROP_PATH = "app.properties";

    private static String version;

    static {
        read();
    }

    private AppPropertiesUtil() {
    }

    private static void read() {
        final Properties properties = new Properties();
        try {
            final InputStream resStream = Launcher.class.getClassLoader().getResourceAsStream(PROP_PATH);
            properties.load(resStream);
            resStream.close();
        } catch (final IOException e) {
            System.err.println("Failed to load EdDoc app properties");
            properties.setProperty("version", "Unknown");
        }
        version = properties.getProperty("version");
    }

    public static String getVersion() {
        return version;
    }

}
