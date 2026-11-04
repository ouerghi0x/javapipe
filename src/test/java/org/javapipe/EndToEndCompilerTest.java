package org.javapipe;

import org.javapipe.compiler.CompilerConfig;
import org.javapipe.compiler.JavaPipeCompiler;

import java.nio.file.Files;
import java.nio.file.Path;

public class EndToEndCompilerTest {
    public static void runTests() throws Exception {
        testMinimalProofOfConcept();
        testMethodReferencesExecution();
        testMethodCallChainingExecution();
        testStreamPipelineExecution();
        testStaticMethodsExecution();
        testPrecedenceExecution();
        System.out.println("  ✓ End-to-end compiler tests passed.");
    }

    private static void testMinimalProofOfConcept() throws Exception {
        // From README Section 7:
        // var result = 10 |> x -> x * 2 |> x -> x + 5; -> Expected 25
        String source = """
            public class PocTest {
                public static int output = 0;
                public static void main(String[] args) {
                    var result = 10
                        |> x -> x * 2
                        |> x -> x + 5;
                    output = result;
                    if (result != 25) {
                        throw new RuntimeException("Expected 25, got: " + result);
                    }
                }
            }
            """;
        runTestSnippet("PocTest", source);
    }

    private static void testMethodReferencesExecution() throws Exception {
        // From README Section 1:
        // var result = " hello world " |> String::trim |> String::toUpperCase;
        String source = """
            public class MethodRefTest {
                public static void main(String[] args) {
                    var result = " hello world "
                        |> String::trim
                        |> String::toUpperCase;
                    if (!"HELLO WORLD".equals(result)) {
                        throw new RuntimeException("Expected 'HELLO WORLD', got: " + result);
                    }
                }
            }
            """;
        runTestSnippet("MethodRefTest", source);
    }

    private static void testMethodCallChainingExecution() throws Exception {
        // From README Section 2:
        // var result = "hello" |> replace("h", "H") |> substring(0, 3);
        String source = """
            public class MethodCallTest {
                public static void main(String[] args) {
                    var result = "hello"
                        |> replace("h", "H")
                        |> substring(0, 3);
                    if (!"Hel".equals(result)) {
                        throw new RuntimeException("Expected 'Hel', got: " + result);
                    }
                }
            }
            """;
        runTestSnippet("MethodCallTest", source);
    }

    private static void testStreamPipelineExecution() throws Exception {
        // From README Section 3:
        String source = """
            import java.util.List;

            public class StreamTest {
                public static void main(String[] args) {
                    var users = List.of("alice", "bob", "charlie");
                    var names = users
                        |> stream()
                        |> filter(name -> name.startsWith("a") || name.startsWith("c"))
                        |> map(String::toUpperCase)
                        |> sorted()
                        |> toList();
                    if (!List.of("ALICE", "CHARLIE").equals(names)) {
                        throw new RuntimeException("Stream pipeline output mismatch: " + names);
                    }
                }
            }
            """;
        runTestSnippet("StreamTest", source);
    }

    private static void testStaticMethodsExecution() throws Exception {
        // From README Section 4:
        String source = """
            public class StaticMethodTest {
                public static void main(String[] args) {
                    var input = "-42";
                    var result = input
                        |> Integer::parseInt
                        |> Math::abs;
                    if (result != 42) {
                        throw new RuntimeException("Expected 42, got: " + result);
                    }
                }
            }
            """;
        runTestSnippet("StaticMethodTest", source);
    }

    private static void testPrecedenceExecution() throws Exception {
        String source = """
            public class PrecedenceTest {
                public static void main(String[] args) {
                    var result = 2 + 3
                        |> x -> x * 10;
                    if (result != 50) {
                        throw new RuntimeException("Expected 50, got: " + result);
                    }
                }
            }
            """;
        runTestSnippet("PrecedenceTest", source);
    }

    private static void runTestSnippet(String className, String source) throws Exception {
        Path tempDir = Files.createTempDirectory("javapipe_test_");
        try {
            Path javapFile = tempDir.resolve(className + ".javap");
            Files.writeString(javapFile, source);

            CompilerConfig config = new CompilerConfig();
            config.setOutputDirectory(tempDir);
            JavaPipeCompiler compiler = new JavaPipeCompiler(config);

            boolean ok = compiler.run(javapFile, new String[0]);
            if (!ok) {
                throw new AssertionError("Execution returned false for " + className);
            }
        } finally {
            // Cleanup
            try (var s = Files.walk(tempDir)) {
                s.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }
}

