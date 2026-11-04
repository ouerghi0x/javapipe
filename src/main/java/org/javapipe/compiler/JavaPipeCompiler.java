package org.javapipe.compiler;

import org.javapipe.ast.CompilationUnitNode;
import org.javapipe.codegen.CodeGenerator;
import org.javapipe.desugar.PipeDesugarer;
import org.javapipe.lexer.Lexer;
import org.javapipe.lexer.SourceLocation;
import org.javapipe.lexer.Token;
import org.javapipe.parser.JavaPipeParser;
import org.javapipe.parser.ParseException;

import javax.tools.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orchestrator compiler for Java Pipe.
 * Transforms .javap source files into valid standard Java, and orchestrates javac compilation.
 */
public class JavaPipeCompiler {
    private final CompilerConfig config;

    public JavaPipeCompiler() {
        this(new CompilerConfig());
    }

    public JavaPipeCompiler(CompilerConfig config) {
        this.config = config != null ? config : new CompilerConfig();
    }

    public String transpile(String sourceCode) {
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();

        JavaPipeParser parser = new JavaPipeParser(tokens);
        CompilationUnitNode unit = parser.parseCompilationUnit();

        PipeDesugarer desugarer = new PipeDesugarer();
        CompilationUnitNode desugaredUnit = desugarer.desugar(unit);

        CodeGenerator generator = new CodeGenerator(desugarer.isNeedsPipeHelper());
        return generator.generate(desugaredUnit);
    }

    public CompilerResult compile(Path sourceFilePath) throws IOException {
        String fileName = sourceFilePath.getFileName().toString();
        String fileBaseName = fileName.replaceAll("\\.[^.]+$", "");
        String sourceContent = Files.readString(sourceFilePath);

        List<CompilerDiagnostic> diagnostics = new ArrayList<>();
        String javaSource;
        try {
            javaSource = transpile(sourceContent);
        } catch (ParseException pe) {
            diagnostics.add(new CompilerDiagnostic(
                CompilerDiagnostic.Kind.ERROR,
                pe.getMessage(),
                pe.getLocation(),
                fileName
            ));
            return new CompilerResult(false, null, null, diagnostics);
        }

        // Detect declared primary class name to satisfy javac requirements
        String topLevelClass = extractTopLevelClassName(javaSource);
        String outputBaseName = topLevelClass != null ? topLevelClass : fileBaseName;

        Path outDir = config.getOutputDirectory();
        Files.createDirectories(outDir);

        Path javaFile;
        if (config.getJavaOutputDirectory() != null) {
            Files.createDirectories(config.getJavaOutputDirectory());
            javaFile = config.getJavaOutputDirectory().resolve(outputBaseName + ".java");
        } else {
            javaFile = outDir.resolve(outputBaseName + ".java");
        }

        // Prevent overwriting source file if compiling a .java file in-place
        if (javaFile.toAbsolutePath().normalize().equals(sourceFilePath.toAbsolutePath().normalize())) {
            Path tempDir = Files.createTempDirectory("javapipe_cache_");
            javaFile = tempDir.resolve(outputBaseName + ".java");
        }

        Files.writeString(javaFile, javaSource);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            diagnostics.add(new CompilerDiagnostic(
                CompilerDiagnostic.Kind.ERROR,
                "No system Java compiler available. Please ensure JDK 17+ is installed (not just JRE).",
                SourceLocation.UNKNOWN,
                fileName
            ));
            return new CompilerResult(false, javaSource, null, diagnostics);
        }

        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, Locale.getDefault(), null)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaFile.toFile());
            List<String> options = List.of("-d", outDir.toAbsolutePath().toString());

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, options, null, compilationUnits);
            boolean success = task.call();

            for (Diagnostic<? extends JavaFileObject> d : diagnosticCollector.getDiagnostics()) {
                CompilerDiagnostic.Kind kind = switch (d.getKind()) {
                    case ERROR -> CompilerDiagnostic.Kind.ERROR;
                    case WARNING, MANDATORY_WARNING -> CompilerDiagnostic.Kind.WARNING;
                    default -> CompilerDiagnostic.Kind.NOTE;
                };
                SourceLocation loc = new SourceLocation((int) d.getLineNumber(), (int) d.getColumnNumber(), (int) d.getPosition());
                diagnostics.add(new CompilerDiagnostic(kind, d.getMessage(Locale.getDefault()), loc, fileName));
            }

            Path classFile = outDir.resolve(outputBaseName + ".class");
            return new CompilerResult(success, javaSource, classFile, diagnostics);
        }
    }

    public boolean run(Path sourceFilePath, String[] args) throws Exception {
        CompilerResult result = compile(sourceFilePath);
        if (!result.isSuccess()) {
            for (CompilerDiagnostic diag : result.getDiagnostics()) {
                System.err.println(diag);
            }
            return false;
        }

        String javaSource = result.getGeneratedJavaSource();
        String className = extractTopLevelClassName(javaSource);
        if (className == null) {
            String fileName = sourceFilePath.getFileName().toString();
            className = fileName.replaceAll("\\.[^.]+$", "");
        }

        String pkg = extractPackage(javaSource);
        String fullClassName = pkg != null ? pkg + "." + className : className;

        Path outDir = config.getOutputDirectory().toAbsolutePath();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{outDir.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> clazz = classLoader.loadClass(fullClassName);
            Method mainMethod = clazz.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) (args != null ? args : new String[0]));
            return true;
        }
    }

    private String extractTopLevelClassName(String javaSource) {
        Pattern pattern = Pattern.compile("(?m)^\\s*(?:public\\s+)?(?:final\\s+)?(?:class|record|interface|enum)\\s+([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(javaSource);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractPackage(String javaSource) {
        for (String line : javaSource.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                return trimmed.substring("package ".length(), trimmed.length() - 1).trim();
            }
        }
        return null;
    }
}
