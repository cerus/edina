package dev.cerus.edina.cli;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.cli.interpreter.Environment;
import dev.cerus.edina.cli.interpreter.Interpreter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(final String[] args) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        final Environment environment = new Environment();
        final Interpreter interpreter = new Interpreter(environment);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("!stack")) {
                System.out.println("[ " + environment.stack().stream()
                        .map(o -> o + "")
                        .collect(Collectors.joining(", ")) + " ]");
                continue;
            }

            try {
                final List<String> lines = Arrays.asList(line.split("\n"));
                final List<Token> tokens = new Tokenizer(lines).tokenize();
                final List<Command> commands = new Parser(lines, tokens).parse();

                commands.forEach(command -> {
                    if (command instanceof Command.RoutineDeclareCommand decl) {
                        environment.declareRoutine(decl);
                    }
                });
                commands.forEach(interpreter::visit);
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
