package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents a Java method reference: target::methodName
 */
public class MethodReferenceNode extends ExpressionNode {
    private final ExpressionNode target;
    private final String methodName;

    public MethodReferenceNode(ExpressionNode target, String methodName, SourceLocation location) {
        super(location);
        this.target = target;
        this.methodName = methodName;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitMethodReference(this, context);
    }
}

