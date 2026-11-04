package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents a lambda expression: parameters -> body
 */
public class LambdaNode extends ExpressionNode {
    private final String parameters;
    private ExpressionNode bodyExpression;
    private final String rawBody; // in case of { block } body

    public LambdaNode(String parameters, ExpressionNode bodyExpression, String rawBody, SourceLocation location) {
        super(location);
        this.parameters = parameters;
        this.bodyExpression = bodyExpression;
        this.rawBody = rawBody;
    }

    public String getParameters() {
        return parameters;
    }

    public ExpressionNode getBodyExpression() {
        return bodyExpression;
    }

    public void setBodyExpression(ExpressionNode bodyExpression) {
        this.bodyExpression = bodyExpression;
    }

    public String getRawBody() {
        return rawBody;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitLambda(this, context);
    }
}

