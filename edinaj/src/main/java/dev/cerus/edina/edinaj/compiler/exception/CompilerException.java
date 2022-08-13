package dev.cerus.edina.edinaj.compiler.exception;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.LocatedException;
import dev.cerus.edina.ast.token.Location;

/**
 * Called when an error occurs during compilation
 */
public class CompilerException extends LocatedException {

    public CompilerException(final Command cmd, final String msg) {
        this(msg, cmd.getOrigin());
    }

    public CompilerException(final Command cmd, final Throwable cause, final String msg) {
        this(msg, cause, cmd.getOrigin());
    }

    public CompilerException(final String message, final Location location) {
        super(message, location);
    }

    public CompilerException(final String message, final Throwable cause, final Location location) {
        super(message, cause, location);
    }

    @Override
    public void printStartBanner() {
        System.out.println();
        super.printStartBanner();
    }

    @Override
    public void printEndBanner() {
        super.printEndBanner();
        System.out.println();
    }

}
