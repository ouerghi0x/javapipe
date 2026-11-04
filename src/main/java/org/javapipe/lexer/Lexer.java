package org.javapipe.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * High-fidelity lexer for Java Pipe source code.
 * Captures whitespace and comments as leading trivia to preserve formatting and line mapping.
 */
public class Lexer {
    private final String source;
    private final int length;
    private int index = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String source) {
        this.source = source != null ? source : "";
        this.length = this.source.length();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        StringBuilder trivia = new StringBuilder();

        while (index < length) {
            char c = peek();

            // Collect whitespace and comments as trivia
            if (Character.isWhitespace(c)) {
                trivia.append(advance());
                continue;
            }

            if (c == '/' && peek(1) == '/') {
                trivia.append(advance()); // /
                trivia.append(advance()); // /
                while (index < length && peek() != '\n') {
                    trivia.append(advance());
                }
                if (index < length && peek() == '\n') {
                    trivia.append(advance());
                }
                continue;
            }

            if (c == '/' && peek(1) == '*') {
                trivia.append(advance()); // /
                trivia.append(advance()); // *
                while (index < length && !(peek() == '*' && peek(1) == '/')) {
                    trivia.append(advance());
                }
                if (index < length) {
                    trivia.append(advance()); // *
                    trivia.append(advance()); // /
                }
                continue;
            }

            // Normal token
            SourceLocation tokenLoc = new SourceLocation(line, column, index);
            String currentTrivia = trivia.toString();
            trivia.setLength(0);

            Token token = scanToken(tokenLoc, currentTrivia);
            tokens.add(token);
        }

        // EOF Token
        SourceLocation eofLoc = new SourceLocation(line, column, index);
        tokens.add(new Token(TokenType.EOF, "", eofLoc, trivia.toString()));
        return tokens;
    }

    private Token scanToken(SourceLocation loc, String leadingTrivia) {
        char c = advance();

        // Check for Pipe operator: |>
        if (c == '|' && peek() == '>') {
            advance();
            return new Token(TokenType.PIPE, "|>", loc, leadingTrivia);
        }

        // Check for Arrow: ->
        if (c == '-' && peek() == '>') {
            advance();
            return new Token(TokenType.ARROW, "->", loc, leadingTrivia);
        }

        // Check for Method Reference: ::
        if (c == ':' && peek() == ':') {
            advance();
            return new Token(TokenType.COLONCOLON, "::", loc, leadingTrivia);
        }

        // Delimiters
        switch (c) {
            case '(': return new Token(TokenType.LPAREN, "(", loc, leadingTrivia);
            case ')': return new Token(TokenType.RPAREN, ")", loc, leadingTrivia);
            case '{': return new Token(TokenType.LBRACE, "{", loc, leadingTrivia);
            case '}': return new Token(TokenType.RBRACE, "}", loc, leadingTrivia);
            case '[': return new Token(TokenType.LBRACKET, "[", loc, leadingTrivia);
            case ']': return new Token(TokenType.RBRACKET, "]", loc, leadingTrivia);
            case ';': return new Token(TokenType.SEMICOLON, ";", loc, leadingTrivia);
            case ',': return new Token(TokenType.COMMA, ",", loc, leadingTrivia);
            case '?': return new Token(TokenType.QUESTION, "?", loc, leadingTrivia);
            case ':': return new Token(TokenType.COLON, ":", loc, leadingTrivia);
            case '~': return new Token(TokenType.TILDE, "~", loc, leadingTrivia);
        }

        // Operators & multi-character operators
        switch (c) {
            case '.':
                return new Token(TokenType.DOT, ".", loc, leadingTrivia);
            case '+':
                if (peek() == '+') { advance(); return new Token(TokenType.PLUSPLUS, "++", loc, leadingTrivia); }
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "+=", loc, leadingTrivia); }
                return new Token(TokenType.PLUS, "+", loc, leadingTrivia);
            case '-':
                if (peek() == '-') { advance(); return new Token(TokenType.MINUSMINUS, "--", loc, leadingTrivia); }
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "-=", loc, leadingTrivia); }
                return new Token(TokenType.MINUS, "-", loc, leadingTrivia);
            case '*':
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "*=", loc, leadingTrivia); }
                return new Token(TokenType.STAR, "*", loc, leadingTrivia);
            case '/':
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "/=", loc, leadingTrivia); }
                return new Token(TokenType.SLASH, "/", loc, leadingTrivia);
            case '%':
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "%=", loc, leadingTrivia); }
                return new Token(TokenType.PERCENT, "%", loc, leadingTrivia);
            case '&':
                if (peek() == '&') { advance(); return new Token(TokenType.AND, "&&", loc, leadingTrivia); }
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "&=", loc, leadingTrivia); }
                return new Token(TokenType.AMP, "&", loc, leadingTrivia);
            case '|':
                if (peek() == '|') { advance(); return new Token(TokenType.OR, "||", loc, leadingTrivia); }
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "|=", loc, leadingTrivia); }
                return new Token(TokenType.BAR, "|", loc, leadingTrivia);
            case '^':
                if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "^=", loc, leadingTrivia); }
                return new Token(TokenType.CARET, "^", loc, leadingTrivia);
            case '!':
                if (peek() == '=') { advance(); return new Token(TokenType.NE, "!=", loc, leadingTrivia); }
                return new Token(TokenType.BANG, "!", loc, leadingTrivia);
            case '=':
                if (peek() == '=') { advance(); return new Token(TokenType.EQ, "==", loc, leadingTrivia); }
                return new Token(TokenType.ASSIGN, "=", loc, leadingTrivia);
            case '<':
                if (peek() == '<') {
                    advance();
                    if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, "<<=", loc, leadingTrivia); }
                    return new Token(TokenType.LSHIFT, "<<", loc, leadingTrivia);
                }
                if (peek() == '=') { advance(); return new Token(TokenType.LE, "<=", loc, leadingTrivia); }
                return new Token(TokenType.LT, "<", loc, leadingTrivia);
            case '>':
                if (peek() == '>') {
                    advance();
                    if (peek() == '>') {
                        advance();
                        if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, ">>>=", loc, leadingTrivia); }
                        return new Token(TokenType.URSHIFT, ">>>", loc, leadingTrivia);
                    }
                    if (peek() == '=') { advance(); return new Token(TokenType.COMPOUND_ASSIGN, ">>=", loc, leadingTrivia); }
                    return new Token(TokenType.RSHIFT, ">>", loc, leadingTrivia);
                }
                if (peek() == '=') { advance(); return new Token(TokenType.GE, ">=", loc, leadingTrivia); }
                return new Token(TokenType.GT, ">", loc, leadingTrivia);
        }

        // Strings & Text Blocks
        if (c == '"') {
            if (peek() == '"' && peek(1) == '"') {
                // Text block: """
                advance(); advance();
                StringBuilder sb = new StringBuilder("\"\"\"");
                while (index < length && !(peek() == '"' && peek(1) == '"' && peek(2) == '"')) {
                    if (peek() == '\\') {
                        sb.append(advance());
                        if (index < length) sb.append(advance());
                    } else {
                        sb.append(advance());
                    }
                }
                if (index < length) {
                    sb.append(advance());
                    sb.append(advance());
                    sb.append(advance());
                }
                return new Token(TokenType.TEXT_BLOCK_LITERAL, sb.toString(), loc, leadingTrivia);
            } else {
                // Standard String
                StringBuilder sb = new StringBuilder("\"");
                while (index < length && peek() != '"') {
                    if (peek() == '\\') {
                        sb.append(advance());
                        if (index < length) sb.append(advance());
                    } else if (peek() == '\n') {
                        break; // unterminated string
                    } else {
                        sb.append(advance());
                    }
                }
                if (index < length && peek() == '"') {
                    sb.append(advance());
                }
                return new Token(TokenType.STRING_LITERAL, sb.toString(), loc, leadingTrivia);
            }
        }

        // Char literal
        if (c == '\'') {
            StringBuilder sb = new StringBuilder("'");
            while (index < length && peek() != '\'') {
                if (peek() == '\\') {
                    sb.append(advance());
                    if (index < length) sb.append(advance());
                } else if (peek() == '\n') {
                    break;
                } else {
                    sb.append(advance());
                }
            }
            if (index < length && peek() == '\'') {
                sb.append(advance());
            }
            return new Token(TokenType.CHAR_LITERAL, sb.toString(), loc, leadingTrivia);
        }

        // Number literals
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            boolean isHex = false;
            boolean isBin = false;
            if (c == '0' && (peek() == 'x' || peek() == 'X')) {
                isHex = true;
                sb.append(advance());
            } else if (c == '0' && (peek() == 'b' || peek() == 'B')) {
                isBin = true;
                sb.append(advance());
            }

            boolean isFloat = false;
            while (index < length) {
                char next = peek();
                if (isHex && ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f') || (next >= 'A' && next <= 'F') || next == '_')) {
                    sb.append(advance());
                } else if (isBin && (next == '0' || next == '1' || next == '_')) {
                    sb.append(advance());
                } else if (Character.isDigit(next) || next == '_') {
                    sb.append(advance());
                } else if (next == '.' && Character.isDigit(peek(1)) && !isHex && !isBin) {
                    isFloat = true;
                    sb.append(advance());
                } else if ((next == 'e' || next == 'E') && !isHex) {
                    isFloat = true;
                    sb.append(advance());
                    if (peek() == '+' || peek() == '-') sb.append(advance());
                } else if (next == 'f' || next == 'F' || next == 'd' || next == 'D') {
                    isFloat = true;
                    sb.append(advance());
                    break;
                } else if (next == 'l' || next == 'L') {
                    sb.append(advance());
                    break;
                } else {
                    break;
                }
            }
            return new Token(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL, sb.toString(), loc, leadingTrivia);
        }

        // Identifiers & Keywords
        if (Character.isJavaIdentifierStart(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            while (index < length && Character.isJavaIdentifierPart(peek())) {
                sb.append(advance());
            }
            String id = sb.toString();

            if (id.equals("_")) {
                return new Token(TokenType.PLACEHOLDER, id, loc, leadingTrivia);
            }
            if (id.equals("true") || id.equals("false")) {
                return new Token(TokenType.BOOLEAN_LITERAL, id, loc, leadingTrivia);
            }
            if (id.equals("null")) {
                return new Token(TokenType.NULL_LITERAL, id, loc, leadingTrivia);
            }
            return new Token(TokenType.IDENTIFIER, id, loc, leadingTrivia);
        }

        // Fallback single character
        return new Token(TokenType.IDENTIFIER, String.valueOf(c), loc, leadingTrivia);
    }

    private char advance() {
        if (index >= length) return '\0';
        char c = source.charAt(index++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int offset) {
        int pos = index + offset;
        if (pos >= length) return '\0';
        return source.charAt(pos);
    }
}

