package org.javapipe.lexer;

/**
 * Represents a position in a source file (1-based line and column, 0-based character index).
 */
public record SourceLocation(int line, int column, int offset) {
    public static final SourceLocation UNKNOWN = new SourceLocation(0, 0, 0);

    @Override
    public String toString() {
        return line + ":" + column;
    }
}

