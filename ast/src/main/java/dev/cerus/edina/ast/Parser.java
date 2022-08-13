package dev.cerus.edina.ast;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.ParserException;
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

    private final String fileName;
    private final List<String> sourceLines;
    private final List<Token> tokens;
    private int tokenNum;
    private boolean importAllowed;

    public Parser(final String fileName, final List<String> sourceLines, final List<Token> tokens) {
        this.fileName = fileName;
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
                throw new UnsupportedOperationException();
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
            case PLUS -> new Command.PlusCommand(this.pop().getLocation());
            case MINUS -> new Command.MinusCommand(this.pop().getLocation());
            case DIV -> new Command.DivideCommand(this.pop().getLocation());
            case MULT -> new Command.MultiplyCommand(this.pop().getLocation());
            case MODULO -> new Command.ModuloCommand(this.pop().getLocation());
            case AND -> new Command.AndCommand(this.pop().getLocation());
            case OR -> new Command.OrCommand(this.pop().getLocation());
            case XOR -> new Command.XorCommand(this.pop().getLocation());
            case FLIP -> new Command.FlipCommand(this.pop().getLocation());
            case COLON -> this.parseImportCall();
            case SQUARE_BRACKET_OPEN -> this.parseRoutineAnnotation();
            default -> throw new ParserException("Unexpected token", token.getLocation());
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
        final Token end = this.pop();

        // Look ahead for the routine this is attached to
        if (this.tokenNum >= this.tokens.size() || this.peek().getType() != TokenType.WORD || !this.peek().getValue().equals("rt")) {
            throw new ParserException("Routine annotations can only be declared before routines",
                    start.getLocation().combineWith(this.fileName, this.sourceLines, end.getLocation()));
        }
        if (this.tokenNum >= this.tokens.size() - 1 || this.tokens.get(this.tokenNum + 1).getType() != TokenType.WORD) {
            // Definitely invalid
            throw new ParserException("Unable to determine routine name for annotation",
                    start.getLocation().combineWith(this.fileName, this.sourceLines, end.getLocation()));
        }

        final String routineName = this.tokens.get(this.tokenNum + 2).getValue();
        return new Command.RoutineAnnotationCommand(start.getLocation()
                .combineWith(this.fileName, this.sourceLines, end.getLocation()), routineName, elements);
    }

    private Map.Entry<String, Object> parseRoutineAnnotationElement() {
        final Token key = this.pop();
        if (key.getType() != TokenType.WORD) {
            throw new ParserException("Expected WORD, got " + key.getType(), key);
        }
        if (!key.getValue().matches("\\w+")) {
            throw new ParserException("Annotation element key can only contain letters, digits and underscores", key);
        }
        if (this.pop().getType() != TokenType.EQUALS) {
            throw new ParserException("Expected EQUALS, got " + this.prev().getType(), this.prev());
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
            throw new ParserException("Expected container or string, got " + this.peek().getType(), this.pop());
        }
        return Map.entry(keyStr, value);
    }

    private Command parseImportCall() {
        final Token start = this.pop();
        if (this.peek().getType() != TokenType.WORD) {
            throw new ParserException("Expected WORD, found " + this.pop().getType(), this.prev());
        }
        final String importName = this.pop().getValue();
        if (this.peek().getType() != TokenType.DOT) {
            throw new ParserException("Expected DOT, found " + this.pop().getType(), this.prev());
        }
        this.pop();
        if (this.peek().getType() != TokenType.WORD) {
            throw new ParserException("Expected WORD, found " + this.pop().getType(), this.prev());
        }
        final String routineName = this.pop().getValue();
        return new Command.ImportCallCommand(start.getLocation().combineWith(this.fileName,
                this.sourceLines, this.prev().getLocation()), importName, routineName);
    }

    private Command parseRoutineCall() {
        final Token origin = this.pop();

        final Token nameToken = this.pop();
        if (nameToken.getType() != TokenType.WORD) {
            throw new ParserException("Expected WORD, got " + nameToken.getType(), nameToken);
        }
        if (!nameToken.getValue().matches("[a-zA-Z_]+\\w+")) {
            throw new ParserException("Routine name must match [a-zA-Z_]+\\w+", nameToken);
        }
        final String rtName = nameToken.getValue();
        return new Command.RoutineCallCommand(origin.getLocation()
                .combineWith(this.fileName, this.sourceLines, nameToken.getLocation()), rtName);
    }

    private Command parseStringPush() {
        final Token start = this.pop();
        Token end = this.pop();
        boolean backslash = end.getType() == TokenType.ESCAPE;
        while (end.getType() != TokenType.QUOTATION || backslash) {
            backslash = !backslash && end.getType() == TokenType.ESCAPE;
            end = this.pop();
        }

        if (start.getLocation().fromLineNum() != end.getLocation().toLineNum()) {
            throw new ParserException("Multiline strings are not supported", start.getLocation()
                    .combineWith(this.fileName, this.sourceLines, end.getLocation()));
        }

        String str = start.getLocation().firstLine().substring(
                start.getLocation().from() + 1,
                end.getLocation().to() - 1
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

        return new Command.PushCommand(start.getLocation().combineWith(this.fileName, end.getLocation()), str);
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
                case "pop" -> new Command.PopCommand(word.getLocation());
                case "dup" -> new Command.DupCommand(word.getLocation());
                case "swap" -> new Command.SwapCommand(word.getLocation());
                case "over" -> new Command.OverCommand(word.getLocation());
                case "lroll" -> new Command.RollLeftCommand(word.getLocation());
                case "rroll" -> new Command.RollRightCommand(word.getLocation());
                case "end" -> new Command.EndCommand(word.getLocation());
                case "ssize" -> new Command.StackSizeCommand(word.getLocation());
                case "if" -> this.parseIf();
                case "eq", "neq", "gt", "gte", "lt", "lte" -> this.parseComparison();
                case "import" -> this.parseImport();
                case "while", "until" -> this.parseLoop();
                default -> {
                    if (word.getValue().startsWith("native_")) {
                        yield this.parseNativeCall(word);
                    }
                    throw new ParserException("Unknown command", word);
                }
            };
        }
    }

    private Command parseComparison() {
        return new Command.ComparisonCommand(this.prev().getLocation(), switch (this.prev().getValue()) {
            case "eq" -> Command.ComparisonCommand.Type.EQUALS;
            case "neq" -> Command.ComparisonCommand.Type.NOT_EQUALS;
            case "gt" -> Command.ComparisonCommand.Type.GREATER_THAN;
            case "gte" -> Command.ComparisonCommand.Type.GREATER_THAN_EQUALS;
            case "lt" -> Command.ComparisonCommand.Type.LESSER_THAN;
            case "lte" -> Command.ComparisonCommand.Type.LESSER_THAN_EQUALS;
            default -> throw new ParserException("Unknown comparison", this.prev());
        });
    }

    private Command parseLoop() {
        final Token start = this.prev();
        final Command.LoopCommand.Type type = Command.LoopCommand.Type.valueOf(start.getValue().toUpperCase());

        final List<Command> body = new ArrayList<>();
        Command next;
        while (!((next = this.parseCommand()) instanceof Command.EndCommand)) {
            if (next instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParserException("Routines can not be declared inside of loops", illegal.getOrigin());
            }
            body.add(next);
        }
        final Token end = this.prev();

        return new Command.LoopCommand(start.getLocation()
                .combineWith(this.fileName, this.sourceLines, end.getLocation()), type, body);
    }

    private Command parseImport() {
        if (!this.importAllowed) {
            throw new ParserException("Imports must be placed in front of any other commands", this.prev());
        }

        final Token start = this.prev();
        if (this.peek().getType() != TokenType.QUOTATION) {
            throw new ParserException("String expected", this.peek());
        }

        // Kinda hacky, but it works
        final Command strCmd = this.parseStringPush();
        final String importPath = (String) ((Command.PushCommand) strCmd).getParsedValue();
        if (!importPath.matches("(\\w+\\/)*\\w+(\\.edina)?")) {
            throw new ParserException("Invalid import path '" + importPath + "'", strCmd.getOrigin());
        }

        final String[] importSplit = importPath.split("/");
        String name = importSplit[importSplit.length - 1].split("\\.")[0];
        if (this.tokenNum < this.tokens.size() && this.peek().getType() == TokenType.WORD && this.peek().getValue().equals("as")) {
            this.pop();
            if (this.peek().getType() != TokenType.WORD) {
                throw new ParserException("Expected WORD, got " + this.pop().getType(), this.prev());
            }
            name = this.pop().getValue();
            if (!name.matches("\\w+")) {
                throw new ParserException("Unexpected import name", this.prev());
            }
        }
        final Token end = this.prev();

        return new Command.ImportCommand(start.getLocation()
                .combineWith(this.fileName, this.sourceLines, end.getLocation()), importPath, name);
    }

    private Command parseIf() {
        final Token start = this.prev();

        final List<Command> ifBody = new ArrayList<>();
        final List<Command> elseBody = new ArrayList<>();
        while (!this.peek().getValue().equals("end") && !this.peek().getValue().equals("else")) {
            final Command cmd = this.parseCommand();
            if (cmd instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParserException("Routines can not be declared inside of if statements", illegal.getOrigin());
            }
            ifBody.add(cmd);
        }
        if (this.pop().getValue().equals("else")) {
            int i = 0;
            boolean noEnd = false;
            while (!this.peek().getValue().equals("end")) {
                final Command cmd = this.parseCommand();
                if (cmd instanceof Command.RoutineDeclareCommand illegal) {
                    throw new ParserException("Routines can not be declared inside of else statements", illegal.getOrigin());
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
        final Token end = this.prev();

        return new Command.IfCommand(start.getLocation().combineWith(this.fileName, this.sourceLines, end.getLocation()), ifBody, elseBody);
    }

    private Command parseNativeCall(final Token word) {
        final String nativeName = word.getValue().split("_", 2)[1];
        final Command.NativeCallCommand.Type nativeType;
        try {
            nativeType = Command.NativeCallCommand.Type.valueOf(nativeName.toUpperCase());
        } catch (final Exception igored) {
            throw new ParserException("Unknown native call", word);
        }
        return new Command.NativeCallCommand(word.getLocation(), nativeType);
    }

    private Command parseRoutineDeclare() {
        final Token origin = this.prev();

        final Token nameToken = this.pop();
        if (nameToken.getType() != TokenType.WORD) {
            throw new ParserException("Expected WORD, got " + nameToken.getType(), nameToken);
        }
        if (!nameToken.getValue().matches("[a-zA-Z_]+\\w+")) {
            throw new ParserException("Routine name must match [a-zA-Z_]+\\w+", nameToken);
        }
        final String rtName = nameToken.getValue();

        final List<Command> body = new ArrayList<>();
        Command next;
        while (!((next = this.parseCommand()) instanceof Command.EndCommand)) {
            if (next instanceof Command.RoutineDeclareCommand illegal) {
                throw new ParserException("Routines can not be declared inside of routines", illegal.getOrigin());
            }
            body.add(next);
        }

        return new Command.RoutineDeclareCommand(origin.getLocation()
                .combineWith(this.fileName, this.sourceLines, this.prev().getLocation()), rtName, body);
    }

    private Command parseLongPush() {
        boolean negative = false;
        Token prev = this.prev();
        if (prev.getType() == TokenType.MINUS) {
            negative = true;
            prev = this.pop();
        }
        try {
            return new Command.PushCommand(prev.getLocation().combineWith(this.fileName, this.prev().getLocation()),
                    Long.parseLong((negative ? "-" : "") + prev.getValue()));
        } catch (final NumberFormatException ex) {
            throw new ParserException("Failed to parse long", ex, prev.getLocation().combineWith(this.fileName, this.prev().getLocation()));
        }
    }

    private Token peek() {
        if (this.tokenNum >= this.tokens.size()) {
            throw new ParserException("Expected token", this.tokens.get(this.tokenNum - 1));
        }
        return this.tokens.get(this.tokenNum);
    }

    private Token pop() {
        if (this.tokenNum >= this.tokens.size()) {
            throw new ParserException("Token expected", this.prev());
        }
        return this.tokens.get(this.tokenNum++);
    }

    private Token prev() {
        return this.tokens.get(this.tokenNum - 1);
    }

}
