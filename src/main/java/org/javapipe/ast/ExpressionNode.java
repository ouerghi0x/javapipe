package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Base abstract class for expression nodes.
 */
public abstract class ExpressionNode implements AstNode {
    private final SourceLocation location;
    private String leadingTrivia = "";

    public ExpressionNode(SourceLocation location) {
        this.location = location != null ? location : SourceLocation.UNKNOWN;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    public String getLeadingTrivia() {
        return leadingTrivia;
    }

    public void setLeadingTrivia(String leadingTrivia) {
        this.leadingTrivia = leadingTrivia != null ? leadingTrivia : "";
    }
}

