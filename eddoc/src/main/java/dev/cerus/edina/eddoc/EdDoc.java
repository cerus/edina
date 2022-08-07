package dev.cerus.edina.eddoc;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.ParseException;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.eddoc.util.AppPropertiesUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EdDoc {

    private void printBanner() {
        System.out.println("""

                  ___    _ ___             \s
                 | __|__| |   \\ ___  __   \s
                 | _|/ _` | |) / _ \\/ _|  \s
                 |___\\__,_|___/\\___/\\__|\s
                """);
        System.out.println(" v" + AppPropertiesUtil.getVersion());
        System.out.println();
        System.out.println();
    }

    public void generateDocs(final Launcher.Options options) {
        this.printBanner();

        final List<File> inputFiles = this.aggregateInputFiles(options);
        if (inputFiles.isEmpty()) {
            System.out.println("No suitable input files were found! :(");
            return;
        }

        final File outputDir = new File(options.outputDir);
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                System.out.println("Specified output path already exists and is not a directory.");
                return;
            }
            this.deleteDir(outputDir);
        }
        outputDir.mkdirs();

        final List<String> generatedFiles = new ArrayList<>();
        for (final File inputFile : inputFiles) {
            final List<Command> commands = this.readProgram(inputFile);
            final Document document = new Document();
            final String pathDiff;
            try {
                pathDiff = inputFile.getCanonicalPath().replace(new File(".").getCanonicalPath(), "");
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            document.init(pathDiff);
            final FileVisitor fileVisitor = new FileVisitor(document);
            for (final Command command : commands) {
                fileVisitor.visit(command);
            }

            try {
                final File file = new File(outputDir, pathDiff.replace(".edina", ".md"));
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                Files.writeString(file.toPath(), document.finish());
                generatedFiles.add(pathDiff);

                System.out.println(pathDiff + " -> " + file.getPath());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        final File indexFile = new File(outputDir, "index.md");
        final StringBuilder indexBuilder = new StringBuilder();
        indexBuilder.append("# Index\n\n");
        for (final String generatedFile : generatedFiles) {
            final String str = (generatedFile.startsWith("/") ? generatedFile.substring(1) : generatedFile).replace(".edina", ".md");
            indexBuilder.append("- [").append(str.replace(".md", "")).append("](").append(str).append(")\n");
        }
        try {
            Files.writeString(indexFile.toPath(), indexBuilder.toString());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Command> readProgram(final File file) {
        try {
            final List<String> lines = Files.readAllLines(file.toPath());
            final List<Token> tokens = new Tokenizer(lines).tokenize();
            return new Parser(lines, tokens).parse();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read program '" + file.getPath() + "'", e);
        } catch (final ParseException e) {
            throw new RuntimeException("Failed to parse program '" + file.getPath() + "'", e);
        }
    }

    private List<File> aggregateInputFiles(final Launcher.Options options) {
        final File baseInput = new File(options.inputPath);

        final List<File> files = new ArrayList<>();
        if (baseInput.isFile() && baseInput.getName().endsWith(".edina")) {
            files.add(baseInput);
        } else if (baseInput.isDirectory()) {
            files.addAll(this.findAllFilesInDir(baseInput, file -> file.getName().endsWith(".edina")));
        }
        return files;
    }

    private List<File> findAllFilesInDir(final File dir, final Predicate<File> matcher) {
        final List<File> files = new ArrayList<>();
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(this.findAllFilesInDir(file, matcher));
            } else if (matcher.test(file)) {
                files.add(file);
            }
        }
        return files;
    }

    private void deleteDir(final File outputDir) {
        if (outputDir.isFile()) {
            outputDir.delete();
        } else if (outputDir.isDirectory()) {
            for (final File file : outputDir.listFiles()) {
                this.deleteDir(file);
            }
        }
    }

}
