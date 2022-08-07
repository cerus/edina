package dev.cerus.edina.ast.token;

/**
 * Represents a parsed section of a script. This can be a single character or even whole words.
 */
public class Token {

    private final int line;
    private final int from;
    private final int to;
    private final String value;
    private final TokenType type;

    public Token(final int line, final int from, final int to, final String value, final TokenType type) {
        this.line = line;
        this.from = from;
        this.to = to;
        this.value = value;
        this.type = type;
    }

    public static Token of(final TokenType type, final int lineNum, final String line, final int from, final int to) {
        return new Token(lineNum, from, to, line.substring(from, to), type);
    }

    public static Token of(final TokenType type, final int lineNum, final int from, final int to, final String val) {
        return new Token(lineNum, from, to, val, type);
    }

    public int getLine() {
        return this.line;
    }

    public int getFrom() {
        return this.from;
    }

    public int getTo() {
        return this.to;
    }

    public String getValue() {
        return this.value;
    }

    public TokenType getType() {
        return this.type;
    }

}
