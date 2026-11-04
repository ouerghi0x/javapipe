# Java Pipe - Gradle Integration Sample

This sample demonstrates how to build and run a standard Gradle project that uses the **Java Pipe (`|>`)** operator inside ordinary `.java` files.

---

## How It Works

1. The `preprocessJavaPipe` task runs before `compileJava`. It invokes `javapipec --preprocess` to translate all `.java` files with `|>` into standard Java in `build/generated/sources/javapipe/java/main`.
2. The Gradle Java `sourceSets.main.java` is configured to compile from the generated sources directory.
3. Standard `compileJava` compiles standard `.class` bytecode with full incremental build caching!

---

## Building and Running

```bash
# Build the project
gradle build

# Run the application
gradle run
```

Output:
```
=== Running Gradle Project with Java Pipe ===
Even Numbers Squared Sum: 220
🚀 [ POWERED BY JAVA PIPE AND GRADLE ] 🚀
```
