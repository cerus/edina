package dev.cerus.edina.ast.ast;

/**
 * Represents a class that can be visited using a {@link Visitor}
 */
public interface Visitable {

    /**
     * Accept a visiting visitor and return the result of the operation performed by the visitor
     *
     * @param visitor A visitor
     * @param <T>     The visitor type
     *
     * @return The result of the performed operation
     */
    <T> T accept(Visitor<T> visitor);

}
