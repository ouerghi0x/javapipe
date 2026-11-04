package org.javapipe.desugar;

import org.javapipe.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites PipeExpressionNode AST elements into valid standard Java AST nodes.
 */
public class PipeDesugarer implements AstVisitor<AstNode, Void> {
    private boolean needsPipeHelper = false;

    public boolean isNeedsPipeHelper() {
        return needsPipeHelper;
    }

    public CompilationUnitNode desugar(CompilationUnitNode unit) {
        return (CompilationUnitNode) unit.accept(this, null);
    }

    @Override
    public AstNode visitCompilationUnit(CompilationUnitNode node, Void context) {
        CompilationUnitNode transformed = new CompilationUnitNode(node.getLocation());
        transformed.setHasPipeUsage(node.hasPipeUsage());

        for (AstNode child : node.getChildren()) {
            transformed.addChild(child.accept(this, null));
        }
        return transformed;
    }

    @Override
    public AstNode visitRawJavaChunk(RawJavaChunkNode node, Void context) {
        return node;
    }

    @Override
    public AstNode visitPipeExpression(PipeExpressionNode node, Void context) {
        ExpressionNode left = (ExpressionNode) node.getLeft().accept(this, context);
        ExpressionNode right = node.getRight();

        // Case 1: RHS is an unqualified method call -> Receiver invocation (e.g. stream(), replace("h", "H"))
        if (right instanceof MethodCallNode call && call.getTarget() == null) {
            List<ExpressionNode> desugaredArgs = new ArrayList<>();
            for (ExpressionNode arg : call.getArguments()) {
                desugaredArgs.add((ExpressionNode) arg.accept(this, context));
            }
            return new MethodCallNode(left, call.getMethodName(), desugaredArgs, call.getLocation());
        }

        // Case 2: RHS is a qualified method call -> First-argument or placeholder invocation (e.g. Math.abs())
        if (right instanceof MethodCallNode call && call.getTarget() != null) {
            List<ExpressionNode> args = new ArrayList<>();
            boolean replacedPlaceholder = false;

            for (ExpressionNode arg : call.getArguments()) {
                if (arg instanceof PlaceholderNode) {
                    args.add(left);
                    replacedPlaceholder = true;
                } else {
                    args.add((ExpressionNode) arg.accept(this, context));
                }
            }

            if (!replacedPlaceholder) {
                // Prepend piped value as the first argument
                args.add(0, left);
            }

            return new MethodCallNode(call.getTarget(), call.getMethodName(), args, call.getLocation());
        }

        // Case 3: RHS is a method reference (e.g. String::trim, Integer::parseInt, Math::abs)
        if (right instanceof MethodReferenceNode ref) {
            needsPipeHelper = true;
            List<ExpressionNode> args = List.of(left, ref);
            return new MethodCallNode(null, "__pipe", args, node.getLocation());
        }

        // Case 4: RHS is a lambda expression (e.g. x -> x * 2)
        if (right instanceof LambdaNode lambda) {
            needsPipeHelper = true;
            LambdaNode desugaredLambda = (LambdaNode) lambda.accept(this, context);
            List<ExpressionNode> args = List.of(left, desugaredLambda);
            return new MethodCallNode(null, "__pipe", args, node.getLocation());
        }

        // Case 5: RHS contains placeholder _ in an expression
        if (hasPlaceholder(right)) {
            return replacePlaceholder(right, left);
        }

        // Case 6: General expression -> pipe function application
        needsPipeHelper = true;
        ExpressionNode desugaredRight = (ExpressionNode) right.accept(this, context);
        List<ExpressionNode> args = List.of(left, desugaredRight);
        return new MethodCallNode(null, "__pipe", args, node.getLocation());
    }

    private boolean hasPlaceholder(ExpressionNode expr) {
        if (expr instanceof PlaceholderNode) return true;
        if (expr instanceof ParenExpressionNode paren) return hasPlaceholder(paren.getInner());
        if (expr instanceof BinaryOpNode bin) return hasPlaceholder(bin.getLeft()) || hasPlaceholder(bin.getRight());
        return false;
    }

    private ExpressionNode replacePlaceholder(ExpressionNode expr, ExpressionNode replacement) {
        if (expr instanceof PlaceholderNode) return replacement;
        if (expr instanceof ParenExpressionNode paren) {
            return new ParenExpressionNode(replacePlaceholder(paren.getInner(), replacement), paren.getLocation());
        }
        if (expr instanceof BinaryOpNode bin) {
            return new BinaryOpNode(
                replacePlaceholder(bin.getLeft(), replacement),
                bin.getOperator(),
                replacePlaceholder(bin.getRight(), replacement),
                bin.getLocation()
            );
        }
        return expr;
    }

    @Override
    public AstNode visitMethodCall(MethodCallNode node, Void context) {
        ExpressionNode target = node.getTarget() != null ? (ExpressionNode) node.getTarget().accept(this, context) : null;
        List<ExpressionNode> args = new ArrayList<>();
        for (ExpressionNode arg : node.getArguments()) {
            args.add((ExpressionNode) arg.accept(this, context));
        }
        return new MethodCallNode(target, node.getMethodName(), args, node.getLocation());
    }

    @Override
    public AstNode visitMethodReference(MethodReferenceNode node, Void context) {
        return node;
    }

    @Override
    public AstNode visitLambda(LambdaNode node, Void context) {
        ExpressionNode body = node.getBodyExpression() != null ? (ExpressionNode) node.getBodyExpression().accept(this, context) : null;
        return new LambdaNode(node.getParameters(), body, node.getRawBody(), node.getLocation());
    }

    @Override
    public AstNode visitLiteral(LiteralNode node, Void context) {
        return node;
    }

    @Override
    public AstNode visitIdentifier(IdentifierNode node, Void context) {
        return node;
    }

    @Override
    public AstNode visitBinaryOp(BinaryOpNode node, Void context) {
        ExpressionNode left = (ExpressionNode) node.getLeft().accept(this, context);
        ExpressionNode right = (ExpressionNode) node.getRight().accept(this, context);
        return new BinaryOpNode(left, node.getOperator(), right, node.getLocation());
    }

    @Override
    public AstNode visitUnaryOp(UnaryOpNode node, Void context) {
        ExpressionNode operand = (ExpressionNode) node.getOperand().accept(this, context);
        return new UnaryOpNode(node.getOperator(), operand, node.isPrefix(), node.getLocation());
    }

    @Override
    public AstNode visitParenExpression(ParenExpressionNode node, Void context) {
        ExpressionNode inner = (ExpressionNode) node.getInner().accept(this, context);
        return new ParenExpressionNode(inner, node.getLocation());
    }

    @Override
    public AstNode visitPlaceholder(PlaceholderNode node, Void context) {
        return node;
    }
}

