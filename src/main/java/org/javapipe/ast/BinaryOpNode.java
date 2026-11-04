package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents binary operations: +, -, *, /, ==, !=, &&, ||, etc.
 */
public class BinaryOpNode extends ExpressionNode {
    private ExpressionNode left;
    private final String operator;
    private ExpressionNode right;

    public BinaryOpNode(ExpressionNode left, String operator, ExpressionNode right, SourceLocation location) {
        super(location);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public void setLeft(ExpressionNode left) {
        this.left = left;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    public void setRight(ExpressionNode right) {
        this.right = right;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitBinaryOp(this, context);
    }
}

