package org.javapipe.compiler;

import org.javapipe.lexer.SourceLocation;

/**
 * Diagnostic reporting compilation errors or warnings.
 */
public record CompilerDiagnostic(Kind kind, String message, SourceLocation location, String sourceFile) {
    public enum Kind {
        ERROR,
        WARNING,
        NOTE
    }

    @Override
    public String toString() {
        return String.format("[%s] %s%s: %s",
            kind,
            sourceFile != null ? sourceFile + ":" : "",
            location != null ? location.toString() : "",
            message
        );
    }
}

