package dev.cerus.edina.ast;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.ParseException;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a collection of tokens into an abstract syntax tree (aka a collection of commands)
 */
public class Parser {

    private final List<String> sourceLines;
    private final List<Token> tokens;
    private int tokenNum;
    private boolean importAllowed;

    public Parser(final List<String> sourceLines, final List<Token> tokens) {
        this.sourceLines = sourceLines;
        this.tokens = tokens;
    }

    /**
     * Attempt to parse the tokens into an ast
     *
     * @return A list of parsed commands
     */
    public List<Command> parse() {
        this.importAllowed = true;
        final List<Command> commands = new ArrayList<>();
        while (this.tokenNum < this.tokens.size()) {
            final Command command = this.parseCommand();
            if (command == null) {
                throw new ParseException("Unknown token", new Token(0, 0, 1, "", null));
            }
            if (!(command instanceof Command.ImportCommand)) {
                this.importAllowed = false;
            }
            commands.add(command);
        }
        return commands;
    }

    private Command parseCommand() {
        final Token token = this.peek();
        return switch (token.getType()) {
            case DOT -> this.parseRoutineCall();
            case QUOTATION -> this.parseStringPush();
            case WORD -> this.parseWord();
            case PLUS -> new Command.PlusCommand(this.pop());
            case MINUS -> new Command.MinusCommand(this.pop());
            case DIV -> new Command.DivideCommand(this.pop());
            case MULT -> new Command.MultiplyCommand(this.pop());
            case MODULO -> new Command.ModuloCommand(this.pop());
            case AND -> new Command.AndCommand(this.pop());
            case OR -> new Command.OrCommand(this.pop());
            case XOR -> new Command.XorCommand(this.pop());
            case FLIP -> new Command.FlipCommand(this.pop());
            case COLON -> this.parseImportCall();
            case SQUARE_BRACKET_OPEN -> this.parseRoutineAnnotation();
            default -> throw new ParseException("Unexpected token", token);
        };
    }

    private Command parseRoutineAnnotation() {
        final Token start = this.pop();

        // Parse everything inside the [ ]
        final Map<String, Object> elements = new HashMap<>();
        while (this.peek().getType() != TokenType.SQUARE_BRACKET_CLOSE) {
            final Map.Entry<String, Object> elem = this.parseRoutineAnnotationElement();
            elements.put(elem.getKey(), elem.getValue());
        }
        this.pop();

        // Look ahead for the routine this is attached to
        if (this.peek().getType() != TokenType.WORD || !this.peek().getValue().equals("rt")) {
            throw new ParseException("Routine annotations can only be declared before routines", this.pop());
        }
        if (this.tokenNum >= this.tokens.size() - 1 || this.tokens.get(this.tokenNum + 1).getType() != TokenType.WORD) {
            // Definitely invalid
            throw new ParseException("Unable to determine routine name for annotation", start);
        }

        final String routineName = this.tokens.get(this.tokenNum + 2).getValue();
        return new Command.RoutineAnnotationCommand(start, routineName, elements);
    }

    private Map.Entry<String, Object> parseRoutineAnnotationElement() {
        final Token key = this.pop();
        if (key.getType() != TokenType.WORD) {
            throw new ParseException("Expected WORD, got " + key.getType(), key);
        }
        if (!key.getValue().matches("\\w+")) {
            throw new ParseException("Annotation element key can only contain letters, digits and underscores", key);
        }
        if (this.pop().getType() != TokenType.EQUALS) {
            throw new ParseException("Expected EQUALS, got " + this.prev().getType(), this.prev());
        }

        final String keyStr = key.getValue();
        final Object value;
        if (this.peek().getType() == TokenType.CURLY_BRACKET_OPEN) {
            // Parse container
            this.pop();
            final Map<String, Object> elements = new HashMap<>();
            while (this.peek().getType() != TokenType.CURLY_BRACKET_CLOSE) {
                final Map.Entry<String, Object> elem = this.parseRoutineAnnotationElement();
                elements.put(elem.getKey(), elem.getValue());
            }
            this.pop();
            value = elements;
        } else if (this.peek().getType() == TokenType.WORD) {
            // Simple string
            value = this.pop().getValue();
        } else if (this.peek().getType() == TokenType.QUOTATION) {
            // Complex string
            value = ((Command.PushCommand) this.parseStringPush()).getParsedValue();
        } else {
            throw new ParseException("Expected container or string, got " + this.peek().getType(), this.pop());
        }
        return Map.entry(keyStr, value);
    }

    private Command parseImportCall() {
        final Token start = this.pop();
        if (this.peek().getType() != TokenType.WORD) {
            throw new ParseException("Expected WORD, found " + this.pop().getType(), this.prev());
        }
        final String importName = this.pop().getValue();
        if (this.peek().getType() != TokenType.DOT) {
            throw new ParseException("Expected DOT, found " + this.pop().getType(), this.prev());
        }
        this.pop();
        if (this.peek().getType() != TokenType.WORD) {
            throw new ParseException("Expected WORD, found " + this.pop().getType(), this.prev());
        }
        final String routineName = this.pop().getValue();
        return new Command.ImportCallCommand(start, importName, routineName);
    }

    private Command parseRoutineCall() {
        final Token origin = this.pop();

        final Token nameToken = this.pop();
        if (nameToken.getType() != TokenType.WORD) {
            throw new ParseException("Expected WORD, got " + nameToken.getType(), nameToken);
        }
        if (!nameToken.getValue().matches("[a-zA-Z_]+\\w+")) {
            throw new ParseException("Routine name must match [a-zA-Z_]+\\w+", nameToken);
        }
        final String rtName = nameToken.getValue();
        return new Command.RoutineCallCommand(new Token(origin.getLine(), origin.getFrom(), nameToken.getTo(), "." + nameToken.getValue(), TokenType.WORD), rtName);
    }

    private Command parseStringPush() {
        final Token start = this.pop();
        Token end = this.pop();
        boolean backslash = end.getType() == TokenType.ESCAPE;
        while (end.getType() != TokenType.QUOTATION || backslash) {
            backslash = !backslash && end.getType() == TokenType.ESCAPE;
            end = this.pop();
        }

        if (start.getLine() != end.getLine()) {
            throw new ParseException("Multiline strings are not supported", end);
        }

        String str = this.sourceLines.get(start.getLine() - 1).substring(
                start.getFrom() + 1,
                end.getTo() - 1
        );

        int i = 0;
        boolean escaped = false;
        while (i < str.length()) {
            if (str.charAt(i) == '\\' && !escaped) {
                escaped = true;
            } else if (escaped) {
                switch (str.charAt(i)) {
                    case 'n' -> str = str.substring(0, i - 1) + "\n" + str.substring(i + 1);
                    case 't' -> str = str.substring(0, i - 1) + "\t" + str.substring(i + 1);
                    case 'b' -> str = str.substring(0, i - 1) + "\b" + str.substring(i + 1);
                    case 'r' -> str = str.substring(0, i - 1) + "\r" + str.substring(i + 1);
                    case 'f' -> str = str.substring(0, i - 1) + "\f" + str.substring(i + 1);
                    case 's' -> str = str.substring(0, i - 1) + "\s" + str.substring(i + 1);
                    case '#' -> str = str.substring(0, i - 1) + "#" + str.substring(i + 1);
                    case '\\' -> str = str.substring(0, i - 1) + "\\" + str.substring(i + 1);
                    case '"' -> str = str.substring(0, i - 1) + "\"" + str.substring(i + 1);
                    default -> {
                        // We could throw an error here if we wanted to, but I don't think it's worth it
                    }
                }
                escaped = false;
                i--;
            }
            i++;
        }

        return new Command.PushCommand(new Token(start.getLine(), start.getFrom(), end.getTo(), "\"" + str + "\"", TokenType.WORD), str);
    }

    private Command parseWord() {
        final Token word = this.pop();
        if (word.getValue().matches("-?\\d+")) {
            // Floating point numbers are disabled for now
            /*if (this.tokenNum < this.tokens.size() && this.peek().getType() == TokenType.DOT
                    && this.tokenNum + 1 < this.tokens.size() && this.tokens.get(this.tokenNum + 1).getType() == TokenType.WORD
                    && this.tokens.get(this.tokenNum + 1).getValue().matches("\\d+")) {
                return this.parseDoublePush();
            }*/
            return this.parseLongPush();
        } else {
            return switch (word.getValue()) {
                case "rt" -> this.parseRoutineDeclare();
                case "pop" -> new Command.PopCommand(word);
                case "dup" -> new Command.DupCommand(word);
                case "swap" -> new Command.SwapCommand(word);
                case "over" -> new Command.OverCommand(word);
                case "lroll" -> new Command.RollLeftCommand(word);
                case "rroll" -> new Command.RollRightCommand(word);
                case "end" -> new Command.EndCommand(word);
                case "ssize" -> new Command.StackSizeCommand(word);
                case "ifn", "ifz", "ifgt", "iflt" -> this.parseIf();
                case "import" -> this.parseImport();
                case "while", "until" -> this.parseLoop();
                default -> {
                    if (word.getValue().startsWith("native_")) {
                        yield this.parseNativeCall(word);
                    }
                    throw new ParseException("Unknown command", word);
                }
            };
        }
    }

    private Command parseLoop() {
        final Token start = this.prev();
        final Command.LoopCommand.Type type = Command.LoopCommand.Type.valueOf(start.getValue().toUpperCase());

        final List<Command> body = new ArrayList<>();
        Command next;
        while (!((next = this.parseCommand()) instanceof Command.EndCommand)) {
            if (next instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParseException("Routines can not be declared inside of loops", illegal.getOrigin());
            }
            body.add(next);
        }

        return new Command.LoopCommand(start, type, body);
    }

    private Command parseImport() {
        if (!this.importAllowed) {
            throw new ParseException("Imports must be placed in front of any other commands", this.prev());
        }

        final Token start = this.prev();
        if (this.peek().getType() != TokenType.QUOTATION) {
            throw new ParseException("String expected", this.peek());
        }

        // Kinda hacky, but it works
        final Command strCmd = this.parseStringPush();
        final String importPath = (String) ((Command.PushCommand) strCmd).getParsedValue();
        if (!importPath.matches("(\\w+\\/)*\\w+(\\.edina)?")) {
            throw new ParseException("Invalid import path '" + importPath + "'", strCmd.getOrigin());
        }

        final String[] importSplit = importPath.split("/");
        String name = importSplit[importSplit.length - 1].split("\\.")[0];
        if (this.tokenNum < this.tokens.size() && this.peek().getType() == TokenType.WORD && this.peek().getValue().equals("as")) {
            this.pop();
            if (this.peek().getType() != TokenType.WORD) {
                throw new ParseException("Expected WORD, got " + this.pop().getType(), this.prev());
            }
            name = this.pop().getValue();
            if (!name.matches("\\w+")) {
                throw new ParseException("Unexpected import name", this.prev());
            }
        }

        return new Command.ImportCommand(start, importPath, name);
    }

    private Command parseIf() {
        final Token start = this.prev();

        final List<Command> ifBody = new ArrayList<>();
        final List<Command> elseBody = new ArrayList<>();
        while (!this.peek().getValue().equals("end") && !this.peek().getValue().equals("else")) {
            final Command cmd = this.parseCommand();
            if (cmd instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParseException("Routines can not be declared inside of if statements", illegal.getOrigin());
            }
            ifBody.add(cmd);
        }
        if (this.pop().getValue().equals("else")) {
            int i = 0;
            boolean noEnd = false;
            while (!this.peek().getValue().equals("end")) {
                final Command cmd = this.parseCommand();
                if (cmd instanceof Command.RoutineDeclareCommand illegal) {
                    throw new ParseException("Routines can not be declared inside of else statements", illegal.getOrigin());
                }
                elseBody.add(cmd);
                if (i++ == 0 && cmd instanceof Command.IfCommand) {
                    noEnd = true;
                    break;
                }
            }
            if (!noEnd) {
                this.pop();
            }
        }

        return new Command.IfCommand(start, ifBody, elseBody, Command.IfCommand.Type.valueOf(start.getValue().toUpperCase()));
    }

    private Command parseNativeCall(final Token word) {
        final String nativeName = word.getValue().split("_", 2)[1];
        final Command.NativeCallCommand.Type nativeType;
        try {
            nativeType = Command.NativeCallCommand.Type.valueOf(nativeName.toUpperCase());
        } catch (final Exception igored) {
            throw new ParseException("Unknown native call", word);
        }
        return new Command.NativeCallCommand(word, nativeType);
    }

    private Command parseRoutineDeclare() {
        final Token origin = this.prev();

        final Token nameToken = this.pop();
        if (nameToken.getType() != TokenType.WORD) {
            throw new ParseException("Expected WORD, got " + nameToken.getType(), nameToken);
        }
        if (!nameToken.getValue().matches("[a-zA-Z_]+\\w+")) {
            throw new ParseException("Routine name must match [a-zA-Z_]+\\w+", nameToken);
        }
        final String rtName = nameToken.getValue();

        final List<Command> body = new ArrayList<>();
        Command next;
        while (!((next = this.parseCommand()) instanceof Command.EndCommand)) {
            if (next instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParseException("Routines can not be declared inside of routines", illegal.getOrigin());
            }
            body.add(next);
        }

        return new Command.RoutineDeclareCommand(origin, rtName, body);
    }

    private Command parseLongPush() {
        boolean negative = false;
        Token prev = this.prev();
        if (prev.getType() == TokenType.MINUS) {
            negative = true;
            prev = this.pop();
        }
        try {
            return new Command.PushCommand(prev, Long.parseLong((negative ? "-" : "") + prev.getValue()));
        } catch (final NumberFormatException ex) {
            throw new ParseException("Failed to parse long: " + ex.getMessage(), prev);
        }
    }

    private Command parseDoublePush() {
        boolean negative = false;
        Token prev = this.prev();
        if (prev.getType() == TokenType.MINUS) {
            negative = true;
            prev = this.pop();
        }
        try {
            this.pop();
            final Token end = this.pop();
            return new Command.PushCommand(prev, Double.parseDouble((negative ? "-" : "") + prev.getValue() + "." + end.getValue()));
        } catch (final NumberFormatException ex) {
            throw new ParseException("Failed to parse double: " + ex.getMessage(), prev);
        }
    }

    private Token peek() {
        if (this.tokenNum >= this.tokens.size()) {
            throw new ParseException("Expected token", this.tokens.get(this.tokenNum - 1));
        }
        return this.tokens.get(this.tokenNum);
    }

    private Token pop() {
        if (this.tokenNum >= this.tokens.size()) {
            throw new ParseException("Token expected", this.prev());
        }
        return this.tokens.get(this.tokenNum++);
    }

    private Token prev() {
        return this.tokens.get(this.tokenNum - 1);
    }

}
