package org.javapipe;

import org.javapipe.lexer.Lexer;
import org.javapipe.lexer.Token;
import org.javapipe.lexer.TokenType;

import java.util.List;

public class LexerTest {
    public static void runTests() {
        testBasicPipeToken();
        testMethodReferenceToken();
        testLambdaArrowToken();
        testChainedTokens();
        System.out.println("  ✓ Lexer tests passed.");
    }

    private static void testBasicPipeToken() {
        Lexer lexer = new Lexer("a |> b");
        List<Token> tokens = lexer.tokenize();
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.PIPE, tokens.get(1).getType());
        assertEquals("|>", tokens.get(1).getText());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

    private static void testMethodReferenceToken() {
        Lexer lexer = new Lexer("String::trim");
        List<Token> tokens = lexer.tokenize();
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.COLONCOLON, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
    }

    private static void testLambdaArrowToken() {
        Lexer lexer = new Lexer("x -> x * 2");
        List<Token> tokens = lexer.tokenize();
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.ARROW, tokens.get(1).getType());
        assertEquals("->", tokens.get(1).getText());
    }

    private static void testChainedTokens() {
        Lexer lexer = new Lexer("val |> fn1 |> fn2");
        List<Token> tokens = lexer.tokenize();
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.PIPE, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(TokenType.PIPE, tokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}

