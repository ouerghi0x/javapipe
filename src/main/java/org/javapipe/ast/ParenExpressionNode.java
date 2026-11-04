package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents parenthesized expressions: (expr)
 */
public class ParenExpressionNode extends ExpressionNode {
    private ExpressionNode inner;

    public ParenExpressionNode(ExpressionNode inner, SourceLocation location) {
        super(location);
        this.inner = inner;
    }

    public ExpressionNode getInner() {
        return inner;
    }

    public void setInner(ExpressionNode inner) {
        this.inner = inner;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitParenExpression(this, context);
    }
}

