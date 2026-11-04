package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents an identifier (variable name, type name, keyword identifier).
 */
public class IdentifierNode extends ExpressionNode {
    private final String name;

    public IdentifierNode(String name, SourceLocation location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitIdentifier(this, context);
    }
}

