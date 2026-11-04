package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method call: [target.]methodName(arguments...)
 */
public class MethodCallNode extends ExpressionNode {
    private ExpressionNode target;
    private final String methodName;
    private final List<ExpressionNode> arguments;

    public MethodCallNode(ExpressionNode target, String methodName, List<ExpressionNode> arguments, SourceLocation location) {
        super(location);
        this.target = target;
        this.methodName = methodName;
        this.arguments = arguments != null ? arguments : new ArrayList<>();
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public void setTarget(ExpressionNode target) {
        this.target = target;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitMethodCall(this, context);
    }
}

