package org.javapipe;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running Java Pipe Test Suite");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        try {
            System.out.println("[1/4] Running Lexer Tests...");
            LexerTest.runTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ Lexer tests failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            System.out.println("[2/4] Running Parser Tests...");
            ParserTest.runTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ Parser tests failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            System.out.println("[3/4] Running Desugarer Tests...");
            DesugarerTest.runTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ Desugarer tests failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            System.out.println("[4/4] Running End-to-End Compiler Tests...");
            EndToEndCompilerTest.runTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ End-to-end compiler tests failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.printf("Results: %d suites passed, %d suites failed%n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}

