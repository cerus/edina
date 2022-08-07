package dev.cerus.edina.edinaj.util;

import dev.cerus.edina.edinaj.Launcher;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simple utility for reading the app properties
 */
public class AppPropertiesUtil {

    private static final String PROP_PATH = "app.properties";

    private static String version;

    static {
        read();
    }

    private AppPropertiesUtil() {
    }

    /**
     * Read the embedded properties file
     */
    private static void read() {
        final Properties properties = new Properties();
        try {
            final InputStream resStream = Launcher.class.getClassLoader().getResourceAsStream(PROP_PATH);
            properties.load(resStream);
            resStream.close();
        } catch (final IOException e) {
            System.err.println("Failed to load EdinaJ app properties");
            properties.setProperty("version", "Unknown");
        }
        version = properties.getProperty("version");
    }

    /**
     * Get the EdinaJ version
     *
     * @return The EdinaJ version
     */
    public static String getVersion() {
        return version;
    }

}
