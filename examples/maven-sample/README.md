# Java Pipe - Maven Integration Sample

This sample demonstrates how to build and run a standard Apache Maven project that uses the **Java Pipe (`|>`)** operator inside ordinary `.java` files.

---

## How It Works

1. During Maven's `generate-sources` phase, `exec-maven-plugin` invokes `javapipec --preprocess` to transpile `.java` files into standard Java in `target/generated-sources/javapipe`.
2. The `build-helper-maven-plugin` adds the generated sources directory to the Maven compilation path.
3. Standard `maven-compiler-plugin` compiles the code to regular `.class` bytecode in `target/classes`.

---

## Building and Running

```bash
# Build the project
mvn clean package

# Run the application
mvn exec:java
```

Output:
```
=== Running Maven Project with Java Pipe ===
Input:    42, 18, -99, 105, -7   
Positive Sorted Numbers: [18, 42, 105]
>>> HELLO MAVEN WORLD <<<
```
