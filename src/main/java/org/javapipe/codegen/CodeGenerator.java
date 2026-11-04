package org.javapipe.codegen;

import org.javapipe.ast.*;

import java.util.List;

/**
 * Generates standard, compilable Java source code from the desugared AST.
 */
public class CodeGenerator implements AstVisitor<String, Void> {
    private final boolean injectPipeHelper;

    public CodeGenerator(boolean injectPipeHelper) {
        this.injectPipeHelper = injectPipeHelper;
    }

    public String generate(CompilationUnitNode unit) {
        StringBuilder sb = new StringBuilder();
        for (AstNode child : unit.getChildren()) {
            sb.append(child.accept(this, null));
        }

        String code = sb.toString();
        if (injectPipeHelper && unit.hasPipeUsage()) {
            code = injectHelperMethod(code);
        }
        return code;
    }

    private String injectHelperMethod(String code) {
        int lastBrace = code.lastIndexOf('}');
        if (lastBrace != -1) {
            String helper = "\n    // Synthesized by javapipec for functional pipe expressions\n" +
                            "    private static <__T, __R> __R __pipe(__T __val, java.util.function.Function<__T, __R> __fn) {\n" +
                            "        return __fn.apply(__val);\n" +
                            "    }\n";
            return code.substring(0, lastBrace) + helper + code.substring(lastBrace);
        }
        return code;
    }

    @Override
    public String visitCompilationUnit(CompilationUnitNode node, Void context) {
        return generate(node);
    }

    @Override
    public String visitRawJavaChunk(RawJavaChunkNode node, Void context) {
        return node.getContent();
    }

    @Override
    public String visitPipeExpression(PipeExpressionNode node, Void context) {
        // Fallback if not desugared
        return node.getLeft().accept(this, context) + " |> " + node.getRight().accept(this, context);
    }

    @Override
    public String visitMethodCall(MethodCallNode node, Void context) {
        StringBuilder sb = new StringBuilder();
        if (node.getTarget() != null) {
            sb.append(node.getTarget().accept(this, context)).append(".");
        }
        sb.append(node.getMethodName()).append("(");
        List<ExpressionNode> args = node.getArguments();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(args.get(i).accept(this, context));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitMethodReference(MethodReferenceNode node, Void context) {
        return node.getTarget().accept(this, context) + "::" + node.getMethodName();
    }

    @Override
    public String visitLambda(LambdaNode node, Void context) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getParameters()).append(" -> ");
        if (node.getBodyExpression() != null) {
            sb.append(node.getBodyExpression().accept(this, context));
        } else if (node.getRawBody() != null) {
            sb.append(node.getRawBody());
        }
        return sb.toString();
    }

    @Override
    public String visitLiteral(LiteralNode node, Void context) {
        return node.getRawText();
    }

    @Override
    public String visitIdentifier(IdentifierNode node, Void context) {
        return node.getName();
    }

    @Override
    public String visitBinaryOp(BinaryOpNode node, Void context) {
        return node.getLeft().accept(this, context) + " " + node.getOperator() + " " + node.getRight().accept(this, context);
    }

    @Override
    public String visitUnaryOp(UnaryOpNode node, Void context) {
        if (node.isPrefix()) {
            return node.getOperator() + node.getOperand().accept(this, context);
        } else {
            return node.getOperand().accept(this, context) + node.getOperator();
        }
    }

    @Override
    public String visitParenExpression(ParenExpressionNode node, Void context) {
        return "(" + node.getInner().accept(this, context) + ")";
    }

    @Override
    public String visitPlaceholder(PlaceholderNode node, Void context) {
        return "_";
    }
}

