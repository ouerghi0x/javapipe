package org.javapipe.ast;

import org.javapipe.lexer.SourceLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Root node of an AST representing an entire compilation unit (.javap file).
 */
public class CompilationUnitNode implements AstNode {
    private final List<AstNode> children;
    private final SourceLocation location;
    private boolean hasPipeUsage = false;

    public CompilationUnitNode(SourceLocation location) {
        this.children = new ArrayList<>();
        this.location = location != null ? location : SourceLocation.UNKNOWN;
    }

    public void addChild(AstNode node) {
        if (node != null) {
            this.children.add(node);
        }
    }

    public List<AstNode> getChildren() {
        return children;
    }

    public boolean hasPipeUsage() {
        return hasPipeUsage;
    }

    public void setHasPipeUsage(boolean hasPipeUsage) {
        this.hasPipeUsage = hasPipeUsage;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitCompilationUnit(this, context);
    }
}

