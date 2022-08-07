package dev.cerus.edina.edinaj.compiler.exception;

import dev.cerus.edina.ast.ast.Command;

public class CompilerException extends RuntimeException {

    private final Command culprit;

    public CompilerException(final Command culprit) {
        super("Error during script compilation: '" + culprit.getOrigin().getValue() + "' in line " + culprit.getOrigin().getLine()
                + " (" + culprit.getOrigin().getFrom() + ", " + culprit.getOrigin().getTo() + ")");
        this.culprit = culprit;
    }

    public CompilerException(final Command culprit, final String msg) {
        super("Error during script compilation: '" + culprit.getOrigin().getValue() + "' in line " + culprit.getOrigin().getLine()
                + " (" + culprit.getOrigin().getFrom() + ", " + culprit.getOrigin().getTo() + "): " + msg);
        this.culprit = culprit;
    }

    public CompilerException(final Command culprit, final Throwable t, final String msg) {
        super("Error during script compilation: '" + culprit.getOrigin().getValue() + "' in line " + culprit.getOrigin().getLine()
                + " (" + culprit.getOrigin().getFrom() + ", " + culprit.getOrigin().getTo() + "): " + msg + ": " + t.getMessage(), t);
        this.culprit = culprit;
    }

    public void printDetailedError() {
        System.out.println(this.getMessage());
        System.out.println();
        if (this.getCause() != null) {
            Throwable cause = this;
            while ((cause = cause.getCause()) != null) {
                System.out.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
            }
            System.out.println();
        }
        if (this.culprit != null) {
            System.out.println("   " + this.culprit.getOrigin().getValue());
            if (this.culprit.getOrigin().getValue().length() > 0) {
                if (this.culprit.getOrigin().getValue().length() == 1) {
                    System.out.println("   ^");
                } else {
                    System.out.println("   ^" + "-".repeat(this.culprit.getOrigin().getValue().length() - 2) + "^");
                }
            }
            System.out.println();
        }
    }

    public Command getCulprit() {
        return this.culprit;
    }

}
