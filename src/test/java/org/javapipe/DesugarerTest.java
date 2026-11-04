package org.javapipe;

import org.javapipe.compiler.JavaPipeCompiler;

public class DesugarerTest {
    public static void runTests() {
        testDesugarMethodCalls();
        testDesugarMethodReferences();
        testDesugarLambdas();
        System.out.println("  ✓ Desugarer tests passed.");
    }

    private static void testDesugarMethodCalls() {
        JavaPipeCompiler compiler = new JavaPipeCompiler();
        String code = "public class T { void f() { var x = \"hello\" |> replace(\"h\", \"H\") |> substring(0, 3); } }";
        String desugared = compiler.transpile(code);
        assertTrue(desugared.contains("\"hello\".replace(\"h\", \"H\").substring(0, 3)"));
    }

    private static void testDesugarMethodReferences() {
        JavaPipeCompiler compiler = new JavaPipeCompiler();
        String code = "public class T { void f() { var x = \" hello \" |> String::trim |> String::toUpperCase; } }";
        String desugared = compiler.transpile(code);
        assertTrue(desugared.contains("__pipe"));
        assertTrue(desugared.contains("String::trim"));
        assertTrue(desugared.contains("String::toUpperCase"));
        assertTrue(desugared.contains("private static <__T, __R> __R __pipe"));
    }

    private static void testDesugarLambdas() {
        JavaPipeCompiler compiler = new JavaPipeCompiler();
        String code = "public class T { void f() { var x = 5 |> v -> v * 2 |> v -> v + 10; } }";
        String desugared = compiler.transpile(code);
        assertTrue(desugared.contains("__pipe"));
        assertTrue(desugared.contains("v -> v * 2"));
        assertTrue(desugared.contains("v -> v + 10"));
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: expected true");
        }
    }
}

