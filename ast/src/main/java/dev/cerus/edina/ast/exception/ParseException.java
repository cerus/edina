package dev.cerus.edina.ast.exception;

import dev.cerus.edina.ast.token.Token;

/**
 * Thrown when a syntax error occurs during parsing
 */
public class ParseException extends RuntimeException {

    private final Token culprit;

    public ParseException(final Token culprit) {
        super("Error during script parsing: '" + culprit.getValue() + "' in line " + culprit.getLine() + " (" + culprit.getFrom() + ", " + culprit.getTo() + ")");
        this.culprit = culprit;
    }

    public ParseException(final String message, final Token culprit) {
        super("Error during script parsing: '" + culprit.getValue() + "' in line " + culprit.getLine() + " (" + culprit.getFrom() + ", " + culprit.getTo() + "): " + message);
        this.culprit = culprit;
    }

    /**
     * Print a "detailed" error
     */
    public void printDetailedError() {
        System.out.println(this.getMessage());
        System.out.println();
        if (this.culprit != null) {
            System.out.println("   " + this.culprit.getValue());
            if (this.culprit.getValue().length() > 0) {
                if (this.culprit.getValue().length() == 1) {
                    System.out.println("   ^");
                } else {
                    System.out.println("   ^" + "-".repeat(this.culprit.getValue().length() - 2) + "^");
                }
            }
            System.out.println();
        }
    }

    public Token getCulprit() {
        return this.culprit;
    }

}
