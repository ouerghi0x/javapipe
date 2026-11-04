package org.javapipe.parser;

import org.javapipe.ast.*;
import org.javapipe.lexer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent & Pratt expression parser for Java with the pipe forward operator (|&gt;).
 * Accurately isolates pipe expression ranges and desugars them while preserving surrounding Java.
 */
public class JavaPipeParser {
    private final List<Token> tokens;
    private final int count;
    private int cursor = 0;
    private int endLimit = 0;

    public JavaPipeParser(List<Token> tokens) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.count = this.tokens.size();
        this.endLimit = this.count;
    }

    private static record Range(int start, int end) {}

    public CompilationUnitNode parseCompilationUnit() {
        CompilationUnitNode unit = new CompilationUnitNode(tokens.isEmpty() ? SourceLocation.UNKNOWN : tokens.get(0).getLocation());
        List<Range> pipeRanges = findPipeRanges();

        if (pipeRanges.isEmpty()) {
            StringBuilder allRaw = new StringBuilder();
            for (Token t : tokens) {
                if (t.is(TokenType.EOF)) {
                    allRaw.append(t.getLeadingTrivia());
                } else {
                    allRaw.append(t.getLeadingTrivia()).append(t.getText());
                }
            }
            unit.addChild(new RawJavaChunkNode(allRaw.toString(), SourceLocation.UNKNOWN));
            return unit;
        }

        unit.setHasPipeUsage(true);
        int currentTokenIdx = 0;

        for (Range range : pipeRanges) {
            // Emit raw Java code between currentTokenIdx and range.start()
            if (currentTokenIdx < range.start()) {
                StringBuilder rawBefore = new StringBuilder();
                SourceLocation loc = tokens.get(currentTokenIdx).getLocation();
                for (int i = currentTokenIdx; i < range.start(); i++) {
                    Token t = tokens.get(i);
                    rawBefore.append(t.getLeadingTrivia()).append(t.getText());
                }
                rawBefore.append(tokens.get(range.start()).getLeadingTrivia());
                unit.addChild(new RawJavaChunkNode(rawBefore.toString(), loc));
            }

            // Parse the pipe expression within [range.start(), range.end())
            this.cursor = range.start();
            this.endLimit = range.end();

            ExpressionNode pipeExpr = parseExpression(0);
            unit.addChild(pipeExpr);

            currentTokenIdx = range.end();
        }

        // Emit any trailing raw Java code
        if (currentTokenIdx < count) {
            StringBuilder rawAfter = new StringBuilder();
            SourceLocation loc = tokens.get(currentTokenIdx).getLocation();
            for (int i = currentTokenIdx; i < count; i++) {
                Token t = tokens.get(i);
                if (t.is(TokenType.EOF)) {
                    rawAfter.append(t.getLeadingTrivia());
                } else {
                    rawAfter.append(t.getLeadingTrivia()).append(t.getText());
                }
            }
            if (rawAfter.length() > 0) {
                unit.addChild(new RawJavaChunkNode(rawAfter.toString(), loc));
            }
        }

        return unit;
    }

    private List<Range> findPipeRanges() {
        List<Range> ranges = new ArrayList<>();
        int i = 0;

        while (i < count) {
            Token t = tokens.get(i);
            if (t.is(TokenType.PIPE)) {
                int start = findExpressionStart(i);
                int end = findExpressionEnd(i);
                ranges.add(new Range(start, end));
                i = end;
            } else {
                i++;
            }
        }

        return ranges;
    }

    private int findExpressionStart(int pipeIndex) {
        int depthParen = 0;
        int depthBracket = 0;
        int depthBrace = 0;

        for (int i = pipeIndex - 1; i >= 0; i--) {
            Token t = tokens.get(i);

            if (t.is(TokenType.RPAREN)) depthParen++;
            else if (t.is(TokenType.LPAREN)) {
                if (depthParen > 0) depthParen--;
                else return i + 1;
            }
            else if (t.is(TokenType.RBRACKET)) depthBracket++;
            else if (t.is(TokenType.LBRACKET)) {
                if (depthBracket > 0) depthBracket--;
                else return i + 1;
            }
            else if (t.is(TokenType.RBRACE)) depthBrace++;
            else if (t.is(TokenType.LBRACE)) {
                if (depthBrace > 0) depthBrace--;
                else return i + 1;
            }
            else if (depthParen == 0 && depthBracket == 0 && depthBrace == 0) {
                if (t.isOneOf(TokenType.SEMICOLON, TokenType.ASSIGN, TokenType.COMMA,
                              TokenType.COLON, TokenType.ARROW, TokenType.COMPOUND_ASSIGN)) {
                    return i + 1;
                }
                if (t.is(TokenType.IDENTIFIER)) {
                    String text = t.getText();
                    if (text.equals("return") || text.equals("yield") || text.equals("throw") || text.equals("case")) {
                        return i + 1;
                    }
                }
            }
        }

        return 0;
    }

    private int findExpressionEnd(int pipeIndex) {
        int depthParen = 0;
        int depthBracket = 0;
        int depthBrace = 0;

        for (int i = pipeIndex + 1; i < count; i++) {
            Token t = tokens.get(i);

            if (t.is(TokenType.LPAREN)) depthParen++;
            else if (t.is(TokenType.RPAREN)) {
                if (depthParen > 0) depthParen--;
                else return i;
            }
            else if (t.is(TokenType.LBRACKET)) depthBracket++;
            else if (t.is(TokenType.RBRACKET)) {
                if (depthBracket > 0) depthBracket--;
                else return i;
            }
            else if (t.is(TokenType.LBRACE)) depthBrace++;
            else if (t.is(TokenType.RBRACE)) {
                if (depthBrace > 0) depthBrace--;
                else return i;
            }
            else if (depthParen == 0 && depthBracket == 0 && depthBrace == 0) {
                if (t.isOneOf(TokenType.SEMICOLON, TokenType.COMMA)) {
                    return i;
                }
            }
        }

        return count;
    }

    // =========================================================================
    // Expression Parsing
    // =========================================================================

    public ExpressionNode parseExpression(int minPrecedence) {
        ExpressionNode left = parsePrimaryOrUnary();

        while (!isAtEnd()) {
            Token opToken = peek();
            int prec = getBinaryPrecedence(opToken.getType());
            if (prec <= minPrecedence) {
                break;
            }

            if (opToken.is(TokenType.PIPE)) {
                advance(); // consume '|>'
                ExpressionNode right = parsePipeRhs();
                SourceLocation loc = opToken.getLocation();
                left = new PipeExpressionNode(left, right, loc);
            } else if (isBinaryOperator(opToken.getType())) {
                advance(); // consume operator
                ExpressionNode right = parseExpression(prec);
                left = new BinaryOpNode(left, opToken.getText(), right, opToken.getLocation());
            } else {
                break;
            }
        }

        return left;
    }

    private ExpressionNode parsePipeRhs() {
        Token first = peek();

        // 1. Check for Lambda: identifier -> ...
        if (first.is(TokenType.IDENTIFIER) && peek(1).is(TokenType.ARROW)) {
            Token param = advance();
            advance(); // consume '->'
            ExpressionNode body = parseLambdaBody(getBinaryPrecedence(TokenType.PIPE));
            return new LambdaNode(param.getText(), body, null, first.getLocation());
        }

        // 2. Check for Lambda: (params...) -> ...
        if (first.is(TokenType.LPAREN)) {
            int arrowIndex = findArrowAfterParens(cursor);
            if (arrowIndex != -1) {
                advance(); // consume '('
                StringBuilder params = new StringBuilder();
                while (!isAtEnd() && !peek().is(TokenType.RPAREN)) {
                    Token pt = advance();
                    params.append(pt.getText());
                    if (peek().is(TokenType.COMMA)) {
                        params.append(", ");
                        advance(); // consume ','
                    }
                }
                if (match(TokenType.RPAREN)) {
                    if (match(TokenType.ARROW)) {
                        ExpressionNode body = parseLambdaBody(getBinaryPrecedence(TokenType.PIPE));
                        return new LambdaNode("(" + params + ")", body, null, first.getLocation());
                    }
                }
            }
        }

        // 3. Method reference or Method call or Primary expression
        ExpressionNode primary = parsePrimary();

        // Check if followed by '::' (Method reference, e.g. String::trim)
        if (match(TokenType.COLONCOLON)) {
            Token methodTok = consume(TokenType.IDENTIFIER, "Expected method name after '::'");
            return new MethodReferenceNode(primary, methodTok.getText(), first.getLocation());
        }

        // Check for chained method call on primary
        while (match(TokenType.DOT)) {
            Token methodTok = consume(TokenType.IDENTIFIER, "Expected method name after '.'");
            if (peek().is(TokenType.LPAREN)) {
                List<ExpressionNode> args = parseArgumentList();
                primary = new MethodCallNode(primary, methodTok.getText(), args, methodTok.getLocation());
            } else if (match(TokenType.COLONCOLON)) {
                Token refTok = consume(TokenType.IDENTIFIER, "Expected method name after '::'");
                primary = new MethodReferenceNode(new IdentifierNode(primary.toString() + "." + methodTok.getText(), methodTok.getLocation()), refTok.getText(), methodTok.getLocation());
            } else {
                primary = new IdentifierNode(primary.toString() + "." + methodTok.getText(), methodTok.getLocation());
            }
        }

        return primary;
    }

    private ExpressionNode parseLambdaBody(int minPrecedence) {
        if (peek().is(TokenType.LBRACE)) {
            Token startBrace = advance();
            StringBuilder block = new StringBuilder("{");
            int depth = 1;
            while (!isAtEnd() && depth > 0) {
                Token t = advance();
                if (t.is(TokenType.LBRACE)) depth++;
                else if (t.is(TokenType.RBRACE)) depth--;
                block.append(t.getLeadingTrivia()).append(t.getText());
            }
            return new LambdaNode("", null, block.toString(), startBrace.getLocation());
        }

        return parseExpression(minPrecedence);
    }

    private int findArrowAfterParens(int startIndex) {
        int depth = 0;
        for (int i = startIndex; i < endLimit; i++) {
            Token t = tokens.get(i);
            if (t.is(TokenType.LPAREN)) depth++;
            else if (t.is(TokenType.RPAREN)) {
                depth--;
                if (depth == 0) {
                    if (i + 1 < endLimit && tokens.get(i + 1).is(TokenType.ARROW)) {
                        return i + 1;
                    }
                    return -1;
                }
            } else if (t.is(TokenType.SEMICOLON) || t.is(TokenType.LBRACE)) {
                return -1;
            }
        }
        return -1;
    }

    private ExpressionNode parsePrimaryOrUnary() {
        Token t = peek();

        // Check for inline lambda: identifier -> ...
        if (t.is(TokenType.IDENTIFIER) && peek(1).is(TokenType.ARROW)) {
            Token param = advance();
            advance(); // consume '->'
            ExpressionNode body = parseLambdaBody(0);
            return new LambdaNode(param.getText(), body, null, t.getLocation());
        }

        // Check for inline lambda: (params...) -> ...
        if (t.is(TokenType.LPAREN)) {
            int arrowIndex = findArrowAfterParens(cursor);
            if (arrowIndex != -1) {
                advance(); // consume '('
                StringBuilder params = new StringBuilder();
                while (!isAtEnd() && !peek().is(TokenType.RPAREN)) {
                    Token pt = advance();
                    params.append(pt.getText());
                    if (peek().is(TokenType.COMMA)) {
                        params.append(", ");
                        advance(); // consume ','
                    }
                }
                if (match(TokenType.RPAREN)) {
                    if (match(TokenType.ARROW)) {
                        ExpressionNode body = parseLambdaBody(0);
                        return new LambdaNode("(" + params + ")", body, null, t.getLocation());
                    }
                }
            }
        }

        // Unary operators
        if (t.isOneOf(TokenType.BANG, TokenType.TILDE, TokenType.PLUS, TokenType.MINUS, TokenType.PLUSPLUS, TokenType.MINUSMINUS)) {
            advance();
            ExpressionNode operand = parsePrimaryOrUnary();
            return new UnaryOpNode(t.getText(), operand, true, t.getLocation());
        }

        return parsePrimary();
    }

    private ExpressionNode parsePrimary() {
        Token t = peek();

        if (t.is(TokenType.PLACEHOLDER)) {
            advance();
            return new PlaceholderNode(t.getLocation());
        }

        if (t.isOneOf(TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.STRING_LITERAL,
                      TokenType.TEXT_BLOCK_LITERAL, TokenType.CHAR_LITERAL,
                      TokenType.BOOLEAN_LITERAL, TokenType.NULL_LITERAL)) {
            advance();
            ExpressionNode lit = new LiteralNode(t.getText(), t.getType(), t.getLocation());
            return parseMemberOrCallSuffix(lit);
        }

        if (t.is(TokenType.LPAREN)) {
            advance(); // consume '('
            ExpressionNode inner = parseExpression(0);
            consume(TokenType.RPAREN, "Expected ')' after parenthesized expression");
            ExpressionNode paren = new ParenExpressionNode(inner, t.getLocation());
            return parseMemberOrCallSuffix(paren);
        }

        if (t.is(TokenType.IDENTIFIER)) {
            advance();
            SourceLocation loc = t.getLocation();
            ExpressionNode base;

            if (peek().is(TokenType.LPAREN)) {
                List<ExpressionNode> args = parseArgumentList();
                base = new MethodCallNode(null, t.getText(), args, loc);
            } else {
                base = new IdentifierNode(t.getText(), loc);
            }
            return parseMemberOrCallSuffix(base);
        }

        throw new ParseException("Unexpected token: " + t.getText(), t.getLocation());
    }

    private ExpressionNode parseMemberOrCallSuffix(ExpressionNode base) {
        while (!isAtEnd()) {
            if (match(TokenType.DOT)) {
                Token memberTok = consume(TokenType.IDENTIFIER, "Expected member name after '.'");
                if (peek().is(TokenType.LPAREN)) {
                    List<ExpressionNode> args = parseArgumentList();
                    base = new MethodCallNode(base, memberTok.getText(), args, memberTok.getLocation());
                } else {
                    base = new IdentifierNode(base.toString() + "." + memberTok.getText(), memberTok.getLocation());
                }
            } else if (match(TokenType.COLONCOLON)) {
                Token methodTok = consume(TokenType.IDENTIFIER, "Expected method name after '::'");
                base = new MethodReferenceNode(base, methodTok.getText(), methodTok.getLocation());
            } else if (match(TokenType.PLUSPLUS)) {
                base = new UnaryOpNode("++", base, false, base.getLocation());
            } else if (match(TokenType.MINUSMINUS)) {
                base = new UnaryOpNode("--", base, false, base.getLocation());
            } else {
                break;
            }
        }
        return base;
    }

    private List<ExpressionNode> parseArgumentList() {
        consume(TokenType.LPAREN, "Expected '('");
        List<ExpressionNode> args = new ArrayList<>();
        if (!peek().is(TokenType.RPAREN)) {
            while (true) {
                args.add(parseExpression(0));
                if (match(TokenType.COMMA)) {
                    continue;
                }
                break;
            }
        }
        consume(TokenType.RPAREN, "Expected ')' after argument list");
        return args;
    }

    private int getBinaryPrecedence(TokenType type) {
        return switch (type) {
            case PIPE -> 4;
            case OR -> 5;
            case AND -> 6;
            case BAR -> 7;
            case CARET -> 8;
            case AMP -> 9;
            case EQ, NE -> 10;
            case LT, GT, LE, GE -> 11;
            case LSHIFT, RSHIFT, URSHIFT -> 12;
            case PLUS, MINUS -> 13;
            case STAR, SLASH, PERCENT -> 14;
            default -> 0;
        };
    }

    private boolean isBinaryOperator(TokenType type) {
        return getBinaryPrecedence(type) > 0 && type != TokenType.PIPE;
    }

    private boolean match(TokenType expected) {
        if (!isAtEnd() && peek().is(expected)) {
            advance();
            return true;
        }
        return false;
    }

    private Token consume(TokenType expected, String errorMsg) {
        if (!isAtEnd() && peek().is(expected)) {
            return advance();
        }
        throw new ParseException(errorMsg + " (found: '" + peek().getText() + "')", peek().getLocation());
    }

    private Token advance() {
        if (cursor < endLimit) {
            return tokens.get(cursor++);
        }
        return tokens.get(count - 1);
    }

    private Token peek() {
        return peek(0);
    }

    private Token peek(int offset) {
        int pos = cursor + offset;
        if (pos >= endLimit) return tokens.get(count - 1);
        return tokens.get(pos);
    }

    private boolean isAtEnd() {
        return cursor >= endLimit || peek().is(TokenType.EOF);
    }
}
