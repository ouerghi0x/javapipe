package org.javapipe.tooling;

import org.javapipe.compiler.JavaPipeCompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance batch preprocessor for Maven and Gradle build plugins.
 * Scans source trees, transpiles Java Pipe files, and writes compilable Java to the generated-sources tree.
 */
public class BatchPreprocessor {
    private final JavaPipeCompiler compiler;

    public BatchPreprocessor() {
        this.compiler = new JavaPipeCompiler();
    }

    public static record PreprocessResult(int totalFiles, int transpiledFiles, int unchangedFiles, List<String> errors) {
        public boolean isSuccess() {
            return errors.isEmpty();
        }
    }

    /**
     * Processes all source files in sourceRoot and emits standard .java files into targetRoot.
     */
    public PreprocessResult process(Path sourceRoot, Path targetRoot, boolean copyNonPipeFiles) throws IOException {
        if (!Files.exists(sourceRoot)) {
            return new PreprocessResult(0, 0, 0, List.of("Source directory does not exist: " + sourceRoot));
        }

        Files.createDirectories(targetRoot);

        int[] counts = new int[3]; // [total, transpiled, unchanged]
        List<String> errors = new ArrayList<>();

        try (var stream = Files.walk(sourceRoot)) {
            List<Path> files = stream.filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".javap") || name.endsWith(".jpipe") || name.endsWith(".java");
                })
                .toList();

            for (Path file : files) {
                counts[0]++;
                Path relative = sourceRoot.relativize(file);
                String relativeStr = relative.toString();
                String targetRelativeStr = relativeStr.replaceAll("\\.(javap|jpipe)$", ".java");
                Path targetFile = targetRoot.resolve(targetRelativeStr);

                Files.createDirectories(targetFile.getParent());

                try {
                    String source = Files.readString(file);
                    if (source.contains("|>")) {
                        String transpiled = compiler.transpile(source);
                        Files.writeString(targetFile, transpiled);
                        counts[1]++;
                    } else if (copyNonPipeFiles) {
                        Files.copy(file, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        counts[2]++;
                    }
                } catch (Exception ex) {
                    errors.add("Error processing " + file + ": " + ex.getMessage());
                }
            }
        }

        return new PreprocessResult(counts[0], counts[1], counts[2], errors);
    }

    public static void main(String[] args) {
        Path sourceDir = null;
        Path targetDir = null;
        boolean copyAll = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--source", "-s" -> {
                    if (i + 1 < args.length) sourceDir = Path.of(args[++i]);
                }
                case "--target", "-t" -> {
                    if (i + 1 < args.length) targetDir = Path.of(args[++i]);
                }
                case "--copy-all" -> copyAll = true;
            }
        }

        if (sourceDir == null || targetDir == null) {
            System.err.println("Usage: java org.javapipe.tooling.BatchPreprocessor --source <srcDir> --target <targetDir> [--copy-all]");
            System.exit(1);
        }

        try {
            BatchPreprocessor preprocessor = new BatchPreprocessor();
            PreprocessResult result = preprocessor.process(sourceDir, targetDir, copyAll);

            System.out.printf("[Java Pipe Preprocessor] Processed %d files: %d transpiled with |>, %d clean.%n",
                result.totalFiles(), result.transpiledFiles(), result.unchangedFiles());

            if (!result.isSuccess()) {
                for (String err : result.errors()) {
                    System.err.println("[ERROR] " + err);
                }
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Batch preprocessing failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

