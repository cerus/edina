package dev.cerus.edina.edinac;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.LocatedException;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.edinac.targets.CompilationTarget;
import dev.cerus.edina.edinac.targets.CompilationTargets;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher {

    public static void main(final String[] args) {
        final Options options = new Options();
        final JCommander.Builder builder = JCommander.newBuilder()
                .programName("java -jar edinac.jar")
                .addObject(options);
        final Map<String, CompilationTarget<?, ?>> cmdToTargetMap = new HashMap<>();
        CompilationTargets.getTargets().forEach(compilationTarget -> {
            final dev.cerus.edina.edinac.targets.Options<?, ?> o = compilationTarget.getOptions();
            builder.addCommand(o.getCommandName(), o);
            cmdToTargetMap.put(o.getCommandName(), compilationTarget);
        });
        final JCommander jc = builder.build();

        try {
            jc.parse(args);
        } catch (final Throwable t) {
            if (!(t instanceof MissingCommandException)) {
                System.out.println(t.getMessage());
                jc.usage();
                return;
            }
        }

        final CompilationTarget<?, ?> target = jc.getParsedCommand() == null ? null : cmdToTargetMap.get(jc.getParsedCommand());
        if (target == null) {
            System.out.println("Unknown target");
            return;
        }
        try {
            target.getOptions().getFlavor();
        } catch (final Throwable t) {
            System.out.println("Unknown flavor");
            return;
        }

        final dev.cerus.edina.edinac.targets.Options<?, ?> targetOpts = target.getOptions();

        // Collect all inclusion dirs
        final List<File> inclusions = new ArrayList<>();
        inclusions.add(new File("./"));
        if (System.getenv("EDINA_HOME") != null) {
            inclusions.add(new File(System.getenv("EDINA_HOME")));
        }
        for (final String s : targetOpts.include) {
            inclusions.add(new File(s));
        }
        targetOpts.setIncludedFiles(inclusions);

        final File sourceFile = new File(targetOpts.inputFile);
        try {
            final List<String> lines = Files.readAllLines(sourceFile.toPath());
            final List<Token> tokens = new Tokenizer(sourceFile.getName(), lines).tokenize();
            final List<Command> ast = new Parser(sourceFile.getName(), lines, tokens).parse();
            target.compile(ast);
        } catch (final LocatedException e) {
            e.printDetailedError();
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public static class Options {

    }

}
