package dev.cerus.edina.eddoc;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Launcher {

    public static void main(final String[] args) {
        final Options options = new Options();
        final JCommander jc = JCommander.newBuilder()
                .addObject(options)
                .programName("java -jar eddoc.jar")
                .build();

        try {
            jc.parse(args);
        } catch (final ParameterException e) {
            jc.usage();
            return;
        }

        final EdDoc edDoc = new EdDoc();
        edDoc.generateDocs(options);
    }

    public static class Options {

        @Parameter(
                names = {"--input", "-I"},
                description = "The input file(s). Can either be a path pointing to a .edina file or a directory.",
                required = true
        )
        public String inputPath;

        @Parameter(
                names = {"--output", "-O"},
                description = "The output directory."
        )
        public String outputDir = "./eddoc-out";

    }

}
