package dev.cerus.edina.cli;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.cli.interpreter.Environment;
import dev.cerus.edina.cli.interpreter.Interpreter;
import dev.cerus.edina.edinaj.asm.Stack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Launcher {

    public static void main(final String[] args) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        final Environment environment = new Environment();
        final Interpreter interpreter = new Interpreter(environment);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("!stack")) {
                environment.getStack().debugPrint();
                continue;
            }
            if (line.equalsIgnoreCase("!pop")) {
                environment.getStack().pop();
                continue;
            }
            if (line.equalsIgnoreCase("!string")) {
                final Stack stack = environment.getStack();
                final byte[] arr = stack.popByteArray();
                final byte[] copy = new byte[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    copy[copy.length - 1 - i] = arr[i];
                }
                for (final byte b : copy) {
                    stack.push(b);
                }
                stack.push(copy.length);
                System.out.println(new String(copy));
                continue;
            }

            try {
                final List<String> lines = Arrays.asList(line.split("\n"));
                final List<Token> tokens = new Tokenizer("REPL.edina", lines).tokenize();
                final List<Command> commands = new Parser("REPL.edina", lines, tokens).parse();

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
