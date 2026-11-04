package org.javapipe.compiler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of compiling or transpiling a JavaPipe source file.
 */
public class CompilerResult {
    private final boolean success;
    private final String generatedJavaSource;
    private final Path compiledClassPath;
    private final List<CompilerDiagnostic> diagnostics;

    public CompilerResult(boolean success, String generatedJavaSource, Path compiledClassPath, List<CompilerDiagnostic> diagnostics) {
        this.success = success;
        this.generatedJavaSource = generatedJavaSource;
        this.compiledClassPath = compiledClassPath;
        this.diagnostics = diagnostics != null ? diagnostics : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getGeneratedJavaSource() {
        return generatedJavaSource;
    }

    public Path getCompiledClassPath() {
        return compiledClassPath;
    }

    public List<CompilerDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}

