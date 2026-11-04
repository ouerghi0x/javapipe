package org.javapipe.ast;

public interface AstVisitor<R, C> {
    R visitCompilationUnit(CompilationUnitNode node, C context);
    R visitRawJavaChunk(RawJavaChunkNode node, C context);
    R visitPipeExpression(PipeExpressionNode node, C context);
    R visitMethodCall(MethodCallNode node, C context);
    R visitMethodReference(MethodReferenceNode node, C context);
    R visitLambda(LambdaNode node, C context);
    R visitLiteral(LiteralNode node, C context);
    R visitIdentifier(IdentifierNode node, C context);
    R visitBinaryOp(BinaryOpNode node, C context);
    R visitUnaryOp(UnaryOpNode node, C context);
    R visitParenExpression(ParenExpressionNode node, C context);
    R visitPlaceholder(PlaceholderNode node, C context);
}

