package dev.cerus.edina.edinaj;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.LocatedException;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.edinaj.compiler.Compiler;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.util.AppPropertiesUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Main class
 */
public class EdinaJ {

    private final Launcher.Options options;

    public EdinaJ(final Launcher.Options options) {
        this.options = options;
    }

    /**
     * Print a "EdinaJ" banner to stdout
     */
    private void printBanner() {
        this.println("""

                  ___    _ _              _   \s
                 | __|__| (_)_ _  __ _ _ | |  \s
                 | _|/ _` | | ' \\/ _` | || | \s
                 |___\\__,_|_|_||_\\__,_|\\__/\s
                """);

        this.println(" v" + AppPropertiesUtil.getVersion());
        this.println();
        this.println();
    }

    /**
     * Run the compiler
     *
     * @throws IOException if we can't write the Jar
     */
    public void compile() throws IOException {
        this.printBanner();

        // Collect all inclusion dirs
        final List<File> inclusions = new ArrayList<>();
        inclusions.add(new File("./"));
        if (System.getenv("EDINA_HOME") != null) {
            inclusions.add(new File(System.getenv("EDINA_HOME")));
        }
        for (final String s : this.options.include) {
            inclusions.add(new File(s));
        }

        this.println("Include search directories:");
        for (final File inclusion : inclusions) {
            this.println("  - " + inclusion.getCanonicalPath());
        }
        this.println();

        // Construct compiler
        final File sourceFile = new File(this.options.sourceFilePath);
        final Compiler compiler = new Compiler(new CompilerSettings(
                sourceFile.getName(),
                this.options.packageName,
                this.options.debug,
                this.options.quiet,
                this.options.restricted,
                inclusions,
                this.options.optimizations
        ));

        // Read script lines
        final List<String> lines;
        try {
            lines = Files.readAllLines(sourceFile.toPath());
        } catch (final IOException exception) {
            System.err.println("Failed to read source file: " + exception.getMessage());
            return;
        }

        // Run the compiler
        this.println("Compiling " + this.options.sourceFilePath + " to " + this.options.outputFile + "...");
        try {
            final List<Token> tokens = new Tokenizer(sourceFile.getName(), lines).tokenize();
            final List<Command> ast = new Parser(sourceFile.getName(), lines, tokens).parse();
            for (final Command command : ast) {
                if (command instanceof Command.RoutineDeclareCommand decl) {
                    compiler.addRoutines(decl);
                }
            }
            for (final Command command : ast) {
                compiler.visit(command);
            }
        } catch (final LocatedException e) {
            e.printError();
            System.exit(-1);
            return;
        } catch (final Throwable t) {
            t.printStackTrace();
            System.err.println("Fatal error: " + t.getMessage());
            System.exit(-2);
            return;
        }

        // Write jar
        this.println("Writing Jar...");
        final Map<String, byte[]> classMap = compiler.finish();
        this.createNewJar(this.options, classMap);

        this.println("Done");
        this.println();
        this.println("Success! \\o/");
        this.println(this.options.sourceFilePath + " has been compiled to " + this.options.outputFile + ".");

        if (this.options.run) {
            // Run the compiled Jar
            try {
                new ProcessBuilder("java", "-jar", new File(this.options.outputFile).getAbsolutePath())
                        .inheritIO()
                        .start()
                        .waitFor();
            } catch (final InterruptedException e) {
                throw new RuntimeException("Failed to run Jar file", e);
            }
        }
    }

    /**
     * Write the Jar
     *
     * @param options  The specified options
     * @param classMap The classes to put into the Jar
     *
     * @throws IOException if we can't write the Jar
     */
    private void createNewJar(final Launcher.Options options, final Map<String, byte[]> classMap) throws IOException {
        final File file = new File(options.outputFile);
        try (final FileOutputStream out = new FileOutputStream(file);
             final JarOutputStream jarOut = new JarOutputStream(out)) {
            // Create Manifest
            jarOut.putNextEntry(new ZipEntry("META-INF/"));
            jarOut.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            this.writeZipString(jarOut, """
                    Manifest-Version: 1.0
                    Created-By: EdinaJ v%s (github.com/cerus/edina)
                    Main-Class: %s
                    """.formatted(AppPropertiesUtil.getVersion(), options.packageName + ".Launcher"));

            // Create dirs
            final String packageName = options.packageName.replace(".", "/");
            final StringBuilder full = new StringBuilder();
            for (final String sub : packageName.split("/")) {
                jarOut.putNextEntry(new ZipEntry(full + sub + "/"));
                full.append(sub).append("/");
            }
            // Create classes
            for (final String key : classMap.keySet()) {
                final byte[] classData = classMap.get(key);
                jarOut.putNextEntry(new ZipEntry(key + ".class"));
                jarOut.write(classData, 0, classData.length);
            }
            jarOut.finish();
        }
    }

    /**
     * Write a string into the current zip entry
     *
     * @param zipOut The zip output stream
     * @param str    The string to write
     *
     * @throws IOException if I/O errors occur
     */
    private void writeZipString(final ZipOutputStream zipOut, final String str) throws IOException {
        zipOut.write(str.getBytes(StandardCharsets.UTF_8), 0, str.length());
    }

    private void println() {
        if (!this.options.quiet) {
            System.out.println();
        }
    }

    private void println(final Object obj) {
        if (!this.options.quiet) {
            System.out.println(obj);
        }
    }

}
