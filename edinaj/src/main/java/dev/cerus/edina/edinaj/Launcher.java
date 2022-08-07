package dev.cerus.edina.edinaj;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.util.List;

/**
 * Application bootstrap
 */
public class Launcher {

    public static void main(final String[] args) throws IOException {
        final Options options = new Options();
        final JCommander jc = JCommander.newBuilder()
                .addObject(options)
                .programName("java -jar edinaj.jar")
                .build();

        // Parse options
        try {
            jc.parse(args);
        } catch (final ParameterException e) {
            jc.usage();
            return;
        }

        // Run compiler
        final EdinaJ edinaJ = new EdinaJ(options);
        edinaJ.compile();
    }

    static class Options {

        @Parameter(
                order = 1,
                names = {"--sourcefile", "-F"},
                description = "The source file that should be compiled",
                required = true
        )
        public String sourceFilePath;

        @Parameter(
                order = 2,
                names = {"--outputfile", "-O"},
                description = "The name of the final Jar",
                required = true
        )
        public String outputFile;

        @Parameter(
                order = 3,
                names = {"--package", "-P"},
                description = "The package name that should be used",
                validateValueWith = PackageNameValidator.class
        )
        public String packageName = "dev.cerus.edinalang.compiledscript";

        @Parameter(
                order = 4,
                names = {"--include", "-I"},
                description = "Directories that will be used for imports"
        )
        public List<String> include = List.of();

        @Parameter(
                order = 5,
                names = {"--debug", "-D"},
                description = "Enables debug printing in the final Jar"
        )
        public boolean debug = false;

        @Parameter(
                order = 6,
                names = {"--quiet", "-Q"},
                description = "Suppresses all stdout output if enabled"
        )
        public boolean quiet = false;

        @Parameter(
                order = 7,
                names = {"--run", "-R"},
                description = "Runs the Jar after compilation"
        )
        public boolean run = false;

    }

    public static class PackageNameValidator implements IValueValidator<String> {

        @Override
        public void validate(final String name, final String value) throws ParameterException {
            if (!value.matches("(\\w+\\.)+\\w+")) {
                throw new ParameterException("Invalid package name");
            }
        }

    }

}
