package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents a pipe expression: left |> right
 */
public class PipeExpressionNode extends ExpressionNode {
    private ExpressionNode left;
    private ExpressionNode right;

    public PipeExpressionNode(ExpressionNode left, ExpressionNode right, SourceLocation location) {
        super(location);
        this.left = left;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public void setLeft(ExpressionNode left) {
        this.left = left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    public void setRight(ExpressionNode right) {
        this.right = right;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitPipeExpression(this, context);
    }
}

