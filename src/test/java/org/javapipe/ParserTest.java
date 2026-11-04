package org.javapipe;

import org.javapipe.ast.*;
import org.javapipe.lexer.Lexer;
import org.javapipe.lexer.Token;
import org.javapipe.parser.JavaPipeParser;

import java.util.List;

public class ParserTest {
    public static void runTests() {
        testParsePipeChain();
        testParseMethodReferenceInPipe();
        testParseLambdaInPipe();
        testParsePrecedenceWithArithmetic();
        System.out.println("  ✓ Parser tests passed.");
    }

    private static void testParsePipeChain() {
        String code = "var x = a |> b |> c;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        JavaPipeParser parser = new JavaPipeParser(tokens);
        CompilationUnitNode unit = parser.parseCompilationUnit();

        // Should contain raw chunk "var x = " and an ExpressionNode
        boolean foundPipe = false;
        for (AstNode node : unit.getChildren()) {
            if (node instanceof PipeExpressionNode pipe) {
                foundPipe = true;
                // Outer pipe: (a |> b) |> c
                assertTrue(pipe.getLeft() instanceof PipeExpressionNode);
                assertTrue(pipe.getRight() instanceof IdentifierNode);
            }
        }
        assertTrue(foundPipe);
    }

    private static void testParseMethodReferenceInPipe() {
        String code = "var r = \"hello\" |> String::trim;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        JavaPipeParser parser = new JavaPipeParser(tokens);
        CompilationUnitNode unit = parser.parseCompilationUnit();

        boolean found = false;
        for (AstNode node : unit.getChildren()) {
            if (node instanceof PipeExpressionNode pipe) {
                found = true;
                assertTrue(pipe.getRight() instanceof MethodReferenceNode);
                MethodReferenceNode ref = (MethodReferenceNode) pipe.getRight();
                assertEquals("trim", ref.getMethodName());
            }
        }
        assertTrue(found);
    }

    private static void testParseLambdaInPipe() {
        String code = "var r = 10 |> x -> x * 2;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        JavaPipeParser parser = new JavaPipeParser(tokens);
        CompilationUnitNode unit = parser.parseCompilationUnit();

        boolean found = false;
        for (AstNode node : unit.getChildren()) {
            if (node instanceof PipeExpressionNode pipe) {
                found = true;
                assertTrue(pipe.getRight() instanceof LambdaNode);
                LambdaNode lambda = (LambdaNode) pipe.getRight();
                assertEquals("x", lambda.getParameters());
            }
        }
        assertTrue(found);
    }

    private static void testParsePrecedenceWithArithmetic() {
        String code = "var r = 2 + 3 |> x -> x * 10;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        JavaPipeParser parser = new JavaPipeParser(tokens);
        CompilationUnitNode unit = parser.parseCompilationUnit();

        boolean found = false;
        for (AstNode node : unit.getChildren()) {
            if (node instanceof PipeExpressionNode pipe) {
                found = true;
                assertTrue(pipe.getLeft() instanceof BinaryOpNode);
                BinaryOpNode bin = (BinaryOpNode) pipe.getLeft();
                assertEquals("+", bin.getOperator());
            }
        }
        assertTrue(found);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: expected true");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}

