package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;

/**
 * Represents raw Java code (declarations, statements, imports, blocks)
 * that does not contain pipe operations, preserving line numbers and whitespace.
 */
public class RawJavaChunkNode implements AstNode {
    private final String content;
    private final SourceLocation location;

    public RawJavaChunkNode(String content, SourceLocation location) {
        this.content = content != null ? content : "";
        this.location = location != null ? location : SourceLocation.UNKNOWN;
    }

    public String getContent() {
        return content;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitRawJavaChunk(this, context);
    }
}

