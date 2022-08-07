package dev.cerus.edina.ast.token;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of possible types for tokens
 */
public enum TokenType {

    QUOTATION('"'),
    DOT('.'),
    ESCAPE('\\'),
    PLUS('+'),
    MINUS('-'),
    DIV('/'),
    MULT('*'),
    MODULO('%'),
    AND('&'),
    OR('|'),
    FLIP('~'),
    XOR('^'),
    DOLLAR('$'),
    COLON(':'),
    SQUARE_BRACKET_OPEN('['),
    SQUARE_BRACKET_CLOSE(']'),
    CURLY_BRACKET_OPEN('{'),
    CURLY_BRACKET_CLOSE('}'),
    EQUALS('='),
    WORD;

    private static final Map<Character, TokenType> simpleMap = new HashMap<>();
    private final Character simple;

    TokenType() {
        this(null);
    }

    TokenType(final Character simple) {
        this.simple = simple;
    }

    public static TokenType getSimple(final char c) {
        if (simpleMap.isEmpty()) {
            initializeSimple();
        }
        return simpleMap.get(c);
    }

    private static void initializeSimple() {
        for (final TokenType value : values()) {
            if (value.simple != null) {
                simpleMap.put(value.simple, value);
            }
        }
    }

}
