package dev.cerus.edina.ast.exception;

import dev.cerus.edina.ast.token.Location;
import dev.cerus.edina.ast.token.Token;

/**
 * Thrown when a syntax error occurs during parsing
 */
public class ParseException extends LocatedException {

    public ParseException(final String message, final Token token) {
        this(message, token.getLocation());
    }

    public ParseException(final String message, final Throwable cause, final Location loc) {
        super(message, cause, loc);
    }

    public ParseException(final String message, final Location location) {
        super(message, location);
    }

    @Override
    public void printDetailedError() {
        System.out.println();
        super.printDetailedError();
        System.out.println();
    }

}
