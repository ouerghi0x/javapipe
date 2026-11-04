package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;
import org.javapipe.lexer.TokenType;

/**
 * Represents literal values (numbers, strings, booleans, null).
 */
public class LiteralNode extends ExpressionNode {
    private final String rawText;
    private final TokenType tokenType;

    public LiteralNode(String rawText, TokenType tokenType, SourceLocation location) {
        super(location);
        this.rawText = rawText;
        this.tokenType = tokenType;
    }

    public String getRawText() {
        return rawText;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitLiteral(this, context);
    }
}

