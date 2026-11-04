package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Base interface for all Abstract Syntax Tree nodes in Java Pipe.
 */
public interface AstNode {
    SourceLocation getLocation();
    <R, C> R accept(AstVisitor<R, C> visitor, C context);
}

