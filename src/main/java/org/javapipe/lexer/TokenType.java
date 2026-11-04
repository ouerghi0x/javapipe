package org.javapipe.lexer;

/**
 * Token types supported by the Java Pipe lexer.
 */
public enum TokenType {
    // End of file
    EOF,

    // The core Pipe operator
    PIPE,            // |>

    // Functional & Member navigation
    ARROW,           // ->
    COLONCOLON,      // ::
    DOT,             // .
    COMMA,           // ,
    SEMICOLON,       // ;
    QUESTION,        // ?
    COLON,           // :

    // Delimiters
    LPAREN,          // (
    RPAREN,          // )
    LBRACE,          // {
    RBRACE,          // }
    LBRACKET,        // [
    RBRACKET,        // ]

    // Operators
    ASSIGN,          // =
    PLUS,            // +
    MINUS,           // -
    STAR,            // *
    SLASH,           // /
    PERCENT,         // %
    AMP,             // &
    BAR,             // |
    CARET,           // ^
    TILDE,           // ~
    BANG,            // !
    EQ,              // ==
    NE,              // !=
    LT,              // <
    GT,              // >
    LE,              // <=
    GE,              // >=
    AND,             // &&
    OR,              // ||
    PLUSPLUS,        // ++
    MINUSMINUS,      // --
    LSHIFT,          // <<
    RSHIFT,          // >>
    URSHIFT,         // >>>
    COMPOUND_ASSIGN, // +=, -=, *=, /=, %=, &=, |=, ^=, <<=, >>=, >>>=

    // Literals
    IDENTIFIER,
    STRING_LITERAL,
    TEXT_BLOCK_LITERAL,
    CHAR_LITERAL,
    INT_LITERAL,
    FLOAT_LITERAL,
    BOOLEAN_LITERAL,
    NULL_LITERAL,

    // Comments & Whitespace trivia (stored with tokens for source fidelity)
    COMMENT,

    // Placeholder
    PLACEHOLDER      // _
}

