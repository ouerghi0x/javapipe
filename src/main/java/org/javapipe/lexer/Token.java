package org.javapipe.lexer;

/**
 * A token produced by the Lexer, complete with location and trivia.
 */
public class Token {
    private final TokenType type;
    private final String text;
    private final SourceLocation location;
    private String leadingTrivia;

    public Token(TokenType type, String text, SourceLocation location, String leadingTrivia) {
        this.type = type;
        this.text = text;
        this.location = location;
        this.leadingTrivia = leadingTrivia != null ? leadingTrivia : "";
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public String getLeadingTrivia() {
        return leadingTrivia;
    }

    public void setLeadingTrivia(String leadingTrivia) {
        this.leadingTrivia = leadingTrivia;
    }

    public boolean is(TokenType expected) {
        return this.type == expected;
    }

    public boolean isOneOf(TokenType... expected) {
        for (TokenType t : expected) {
            if (this.type == t) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s['%s'] at %s", type, text, location);
    }
}

