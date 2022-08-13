package dev.cerus.edina.ast.exception;

import java.util.List;

public class MultiLocatedException extends LocatedException {

    private final List<? extends LocatedException> exceptions;

    public MultiLocatedException(final List<? extends LocatedException> exceptions) {
        super(null, null);
        this.exceptions = exceptions;
    }

    @Override
    public void printDetailedError() {
        for (final LocatedException exception : this.exceptions) {
            exception.printDetailedError();
        }
    }

}
