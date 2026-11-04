package org.javapipe.cli;

import org.javapipe.compiler.CompilerConfig;
import org.javapipe.compiler.CompilerDiagnostic;
import org.javapipe.compiler.CompilerResult;
import org.javapipe.compiler.JavaPipeCompiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line interface for the Java Pipe compiler (javapipec).
 */
public class JavaPipeCLI {
    public static final String VERSION = "0.1.0-alpha";

    public static void main(String[] args) {
        int exitCode = runCLI(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int runCLI(String[] args) {
        if (args == null || args.length == 0) {
            printHelp();
            return 1;
        }

        boolean runMode = false;
        boolean transpileMode = false;
        boolean verbose = false;
        Path outputDir = Path.of(".");
        Path javaOutDir = null;
        List<String> files = new ArrayList<>();
        List<String> programArgs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (runMode && !files.isEmpty()) {
                programArgs.add(arg);
                continue;
            }

            switch (arg) {
                case "--help", "-h" -> {
                    printHelp();
                    return 0;
                }
                case "--version", "-v" -> {
                    System.out.println("Java Pipe Compiler (javapipec) version " + VERSION);
                    return 0;
                }
                case "--preprocess", "--batch" -> {
                    // Forward remaining arguments to BatchPreprocessor
                    String[] subArgs = new String[args.length - i - 1];
                    System.arraycopy(args, i + 1, subArgs, 0, subArgs.length);
                    org.javapipe.tooling.BatchPreprocessor.main(subArgs);
                    return 0;
                }
                case "--run", "-r" -> runMode = true;
                case "--transpile", "-E" -> transpileMode = true;
                case "--verbose" -> verbose = true;
                case "-d" -> {
                    if (i + 1 < args.length) {
                        outputDir = Path.of(args[++i]);
                    } else {
                        System.err.println("Error: -d requires a directory path argument.");
                        return 1;
                    }
                }
                case "--emit-java" -> {
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        javaOutDir = Path.of(args[++i]);
                    } else {
                        javaOutDir = outputDir;
                    }
                }
                default -> {
                    if (arg.startsWith("-")) {
                        System.err.println("Unknown option: " + arg);
                        printHelp();
                        return 1;
                    } else {
                        files.add(arg);
                    }
                }
            }
        }

        if (files.isEmpty()) {
            System.err.println("Error: No input files specified.");
            printHelp();
            return 1;
        }

        CompilerConfig config = new CompilerConfig();
        config.setOutputDirectory(outputDir);
        config.setJavaOutputDirectory(javaOutDir);
        config.setVerbose(verbose);

        JavaPipeCompiler compiler = new JavaPipeCompiler(config);

        for (String filePathStr : files) {
            Path filePath = Path.of(filePathStr);
            if (!Files.exists(filePath)) {
                System.err.println("Error: File not found: " + filePathStr);
                return 1;
            }

            if (transpileMode) {
                try {
                    String source = Files.readString(filePath);
                    String transpiled = compiler.transpile(source);
                    System.out.println(transpiled);
                } catch (Exception e) {
                    System.err.println("Transpilation failed for " + filePathStr + ": " + e.getMessage());
                    return 1;
                }
                continue;
            }

            if (runMode) {
                try {
                    boolean success = compiler.run(filePath, programArgs.toArray(new String[0]));
                    return success ? 0 : 1;
                } catch (Exception e) {
                    System.err.println("Execution failed: " + e.getMessage());
                    if (verbose) {
                        e.printStackTrace();
                    }
                    return 1;
                }
            }

            // Normal compile mode
            try {
                CompilerResult result = compiler.compile(filePath);
                for (CompilerDiagnostic diag : result.getDiagnostics()) {
                    if (diag.kind() == CompilerDiagnostic.Kind.ERROR) {
                        System.err.println(diag);
                    } else if (verbose || diag.kind() == CompilerDiagnostic.Kind.WARNING) {
                        System.out.println(diag);
                    }
                }

                if (!result.isSuccess()) {
                    System.err.println("Compilation failed for " + filePathStr);
                    return 1;
                }

                if (verbose) {
                    System.out.println("Successfully compiled: " + filePathStr + " -> " + result.getCompiledClassPath());
                }
            } catch (Exception e) {
                System.err.println("Compilation error for " + filePathStr + ": " + e.getMessage());
                if (verbose) {
                    e.printStackTrace();
                }
                return 1;
            }
        }

        return 0;
    }

    private static void printHelp() {
        System.out.println("""
            Java Pipe Compiler (javapipec) - Extending Java with Functional Pipe Operators (|>)
            
            Usage:
              javapipec [options] <source-files>
            
            Options:
              -r, --run <file> [args...]   Compile and run the main method of the specified file
              -E, --transpile <file>       Print generated standard Java source code to stdout
              --preprocess -s <in> -t <out> Batch transpile source directory tree (for Maven/Gradle)
              -d <directory>               Specify output directory for compiled .class files
              --emit-java [directory]      Write generated .java files to the specified directory
              --verbose                    Enable verbose compiler output
              -v, --version                Print compiler version information
              -h, --help                   Show this help message
            
            Examples:
              javapipec MyPipeline.javap
              javapipec --run MyPipeline.javap
              javapipec --transpile MyPipeline.javap
            """);
    }
}

