package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents the pipe argument placeholder: _
 */
public class PlaceholderNode extends ExpressionNode {
    public PlaceholderNode(SourceLocation location) {
        super(location);
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitPlaceholder(this, context);
    }
}

