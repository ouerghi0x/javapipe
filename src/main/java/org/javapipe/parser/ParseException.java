package org.javapipe.parser;

import org.javapipe.lexer.SourceLocation;

/**
 * Thrown when a syntax error is encountered during parsing.
 */
public class ParseException extends RuntimeException {
    private final SourceLocation location;

    public ParseException(String message, SourceLocation location) {
        super("Syntax error at " + location + ": " + message);
        this.location = location;
    }

    public SourceLocation getLocation() {
        return location;
    }
}

