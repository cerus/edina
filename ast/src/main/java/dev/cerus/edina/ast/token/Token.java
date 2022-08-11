package dev.cerus.edina.ast.token;

/**
 * Represents a parsed section of a script. This can be a single character or even whole words.
 */
public class Token {

    private final Location location;
    private final String value;
    private final TokenType type;

    public Token(final Location location, final String value, final TokenType type) {
        this.location = location;
        this.value = value;
        this.type = type;
    }

    public static Token of(final TokenType type, final int lineNum, final String line, final int from, final int to) {
        return new Token(Location.singleLine(line, lineNum, from, to), line.substring(from, to), type);
    }

    public static Token of(final TokenType type, final int lineNum, final String line, final int from, final int to, final String val) {
        return new Token(Location.singleLine(line, lineNum, from, to), val, type);
    }

    public Location getLocation() {
        return this.location;
    }

    public int getLine() {
        return this.getLocation().fromLineNum();
    }

    public String getValue() {
        return this.value;
    }

    public TokenType getType() {
        return this.type;
    }

}
