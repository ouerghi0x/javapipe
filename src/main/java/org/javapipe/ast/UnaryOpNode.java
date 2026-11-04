package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents unary operations: -, +, !, ++, --
 */
public class UnaryOpNode extends ExpressionNode {
    private final String operator;
    private final ExpressionNode operand;
    private final boolean isPrefix;

    public UnaryOpNode(String operator, ExpressionNode operand, boolean isPrefix, SourceLocation location) {
        super(location);
        this.operator = operator;
        this.operand = operand;
        this.isPrefix = isPrefix;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitUnaryOp(this, context);
    }
}

