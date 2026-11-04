package org.javapipe.compiler;

import java.nio.file.Path;

/**
 * Configuration options for the JavaPipe compiler.
 */
public class CompilerConfig {
    private Path outputDirectory = Path.of(".");
    private boolean emitJava = false;
    private Path javaOutputDirectory = null;
    private boolean verbose = false;

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isEmitJava() {
        return emitJava;
    }

    public void setEmitJava(boolean emitJava) {
        this.emitJava = emitJava;
    }

    public Path getJavaOutputDirectory() {
        return javaOutputDirectory;
    }

    public void setJavaOutputDirectory(Path javaOutputDirectory) {
        this.javaOutputDirectory = javaOutputDirectory;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

