<p align="center">
  <img src="assets/file-icon.svg" width="220" alt="Java Pipe Logo" />
</p>

<h1 align="center">Java Pipe <code>|&gt;</code></h1>

<p align="center">
  <strong>Extending Java with Functional Pipe Operators — Clean, linear, readable pipelines for modern Java.</strong>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License: MIT"></a>
  <img src="https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white" alt="Java 17+"></a>
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg" alt="Build: Passing"></a>
  <img src="https://img.shields.io/badge/VS%20Code-Extension%20Available-007ACC?logo=visualstudiocode&logoColor=white" alt="VS Code Extension"></a>
</p>

---

## 🌟 Vision

**Java Pipe** brings the beloved functional pipe operator (`|>`) from languages like Elixir, F#, and OCaml into the Java ecosystem.

Instead of writing deeply nested method calls like:

```java
var result = Math.abs(Integer.parseInt(String.valueOf(input).trim()));
```

Or cluttering your methods with dozens of single-use intermediate variables, **Java Pipe** lets you express data transformations as clean, left-to-right pipelines:

```java
var result = input
    |> String::valueOf
    |> String::trim
    |> Integer::parseInt
    |> Math::abs;
```

### Core Design Principles

- 🚀 **100% Standard Java & JVM Compatible**: Compiles to standard Java source and bytecode using OpenJDK's `javac`.
- ⚡ **Zero Runtime Overhead**: No custom JVM, no heavy functional libraries, no performance penalty.
- 🔒 **Full Static Type Safety**: Preserves Java's generics, type inference, lambdas, and overload resolution.
- 🛠️ **Developer Experience First**: Includes a standalone CLI compiler (`javapipec`) and a rich VS Code extension.

---

## 📖 Syntax & Features

### 1. Lambdas & Function Application

Pipe expressions directly into anonymous lambda functions:

```java
var result = 10
    |> x -> x * 2
    |> x -> x + 5;
// Result: 25
```

### 2. Method References (Instance & Static)

Pass piped values directly to Java method references:

```java
var greeting = "   hello modern java with pipes!   "
    |> String::trim
    |> String::toUpperCase;
// Result: "HELLO MODERN JAVA WITH PIPES!"
```

```java
var number = "-42"
    |> Integer::parseInt
    |> Math::abs;
// Result: 42
```

### 3. Collections & Java Streams

Chain method calls fluently without breaking syntax:

```java
var topActiveUsers = users
    |> stream()
    |> filter(user -> user.active() && user.score() >= 80)
    |> map(User::name)
    |> sorted()
    |> toList();
```

### 4. Method Calls with Arguments

Pass values into methods as the receiver or first argument:

```java
var result = "hello"
    |> replace("h", "H")
    |> substring(0, 3);
// Result: "Hel"
```

---

## 🚀 Quick Start

### Prerequisites

- **JDK 17 or higher** (`java -version` and `javac -version`).

### Building from Source

Clone and build the compiler:

```bash
git clone https://github.com/javapipe/javapipe.git
cd javapipe
```

- **PowerShell (Windows)**:
  ```powershell
  .\build.ps1
  ```
- **Command Prompt (Windows)**:
  ```batch
  build.bat
  ```
- **Linux / macOS**:
  ```bash
  mkdir -p out bin
  find src/main/java -name "*.java" > sources.txt
  javac -d out @sources.txt
  jar cfe bin/javapipec.jar org.javapipe.cli.JavaPipeCLI -C out .
  ```

This packages the standalone compiler into `bin/javapipec.jar` and runs the automated test suite.

---

## 💻 CLI Usage (`javapipec`)

### Run a `.javap` File Directly

Compiles and executes the file in one step:

```powershell
bin\javapipec.bat --run examples\01_BasicPipe.javap
bin\javapipec.bat --run examples\05_FullPipeline.javap
```

### Transpile to Standard Java

Inspect the clean Java code emitted by the compiler:

```powershell
bin\javapipec.bat --transpile examples\01_BasicPipe.javap
```

Output:

```java
public class BasicPipeExample {
    public static void main(String[] args) {
        var result1 = __pipe(__pipe(10, x -> x * 2), x -> x + 5);
        System.out.println(result1);
    }

    private static <__T, __R> __R __pipe(__T __val, java.util.function.Function<__T, __R> __fn) {
        return __fn.apply(__val);
    }
}
```

### Compile to JVM Bytecode (`.class`)

```powershell
bin\javapipec.bat -d out examples\01_BasicPipe.javap
```

---

## 📦 Using in Maven & Gradle Projects

You can use the pipe operator (`|>`) directly inside ordinary `.java` files in any standard **Maven** or **Gradle** project.

> [!TIP]
> **No local JAR needed!** You can consume Java Pipe directly as an automated dependency via **JitPack**.  
> _(Official Maven Central publishing under `io.github.ouerghi0x:javapipe` is currently in process)._

Working samples are provided in:
- [`examples/springboot-sample/`](examples/springboot-sample): Full Spring Boot 3 REST API using `|>`.
- [`examples/maven-sample/`](examples/maven-sample): Standard Apache Maven integration.
- [`examples/gradle-sample/`](examples/gradle-sample): Standard Gradle integration.

---

### Dependency Setup via JitPack

#### Maven (`pom.xml`)

Add the JitPack repository and dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.ouerghi0x</groupId>
        <artifactId>javapipe</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

#### Gradle (`build.gradle`)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ouerghi0x:javapipe:v0.1.0'
}
```

---

### Build Automation Setup

#### Gradle Integration (`build.gradle`)

```groovy
configurations {
    javapipe
}

dependencies {
    javapipe 'com.github.ouerghi0x:javapipe:v0.1.0'
}

def generatedSources = layout.buildDirectory.dir("generated/sources/javapipe/java/main")

tasks.register('preprocessJavaPipe', JavaExec) {
    classpath = configurations.javapipe
    mainClass = 'org.javapipe.cli.JavaPipeCLI'
    args = ['--preprocess', '--source', file('src/main/java').absolutePath, '--target', generatedSources.get().asFile.absolutePath, '--copy-all']
    inputs.dir('src/main/java')
    outputs.dir(generatedSources)
}

sourceSets.main.java.srcDirs = [generatedSources]
compileJava.dependsOn preprocessJavaPipe
```

#### Maven Integration (`pom.xml`)

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>preprocess-javapipe</id>
            <phase>generate-sources</phase>
            <goals><goal>java</goal></goals>
            <configuration>
                <mainClass>org.javapipe.cli.JavaPipeCLI</mainClass>
                <arguments>
                    <argument>--preprocess</argument>
                    <argument>--source</argument>
                    <argument>${project.basedir}/src/main/java</argument>
                    <argument>--target</argument>
                    <argument>${project.build.directory}/generated-sources/javapipe</argument>
                    <argument>--copy-all</argument>
                </arguments>
                <includePluginDependencies>true</includePluginDependencies>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.github.ouerghi0x</groupId>
            <artifactId>javapipe</artifactId>
            <version>v0.1.0</version>
        </dependency>
    </dependencies>
</plugin>
```

---

## Visual Studio Code Extension

The project includes a complete VS Code extension in [`vscode-extension/`](vscode-extension):

### Features

- **Dedicated Syntax Highlighting**: Custom TextMate grammar for `|>`, `::`, `->`, and `_`.
- **One-Click Execution**: Integrated "Play" button in the editor title bar to run `.javap` files instantly.
- **Split-View Transpilation**: View transpiled Java side-by-side with one click.
- **Productive Snippets**: `jp-stream`, `jp-lambda`, `jp-ref`, and `jp-main`.
- **Custom File Icon**: Transparent vector file icon for `.javap` and `.jpipe` files.

### Installing / Updating the Extension

Simply run:

```batch
update-extension.bat
```

_(Or in PowerShell: `.\update-extension.ps1`)_

Then in VS Code, press **`Ctrl+Shift+P`** and run **`Developer: Reload Window`**.

---

## 🏗️ Compiler Architecture

```
[ .javap Source ]
       │
       ▼
 [ Java Pipe Lexer ]       ──► Preserves comments, whitespace trivia, and detects |>
       │
       ▼
[ Java Pipe AST Parser ]   ──► Pratt precedence-climbing parser isolates pipe expressions
       │
       ▼
 [ AST Desugarer ]         ──► Rewrites |> into idiomatic Java method calls & lambdas
       │
       ▼
 [ Code Generator ]        ──► Emits line-aligned standard Java source code
       │
       ▼
[ javax.tools.JavaCompiler]──► Standard OpenJDK javac compiles directly to .class bytecode
```

For complete technical specifications, grammar precedence tables, and AST design, see the [Technical Architecture Document](technical-architecture.md).

---

## 📁 Repository Structure

```
javawithpipe/
├── assets/                  # Brand logos and vector graphics
├── bin/
│   ├── javapipec.bat        # Windows Batch CLI wrapper
│   └── javapipec.ps1        # PowerShell CLI wrapper
├── examples/                # Example .javap programs
│   ├── 01_BasicPipe.javap
│   ├── 02_MethodReferences.javap
│   ├── 03_StreamsAndCollections.javap
│   ├── 04_StaticMethods.javap
│   └── 05_FullPipeline.javap
├── src/
│   ├── main/java/org/javapipe/
│   │   ├── lexer/           # Lexer & token streams
│   │   ├── ast/             # AST node hierarchy
│   │   ├── parser/          # Pratt expression & statement parser
│   │   ├── desugar/         # Desugaring transformation rules
│   │   ├── codegen/         # Java code generator
│   │   ├── compiler/        # Javac orchestrator & diagnostics
│   │   ├── cli/             # javapipec command line interface
│   │   └── runtime/         # Zero-dependency runtime utilities
│   └── test/java/org/javapipe/
│       ├── LexerTest.java
│       ├── ParserTest.java
│       ├── DesugarerTest.java
│       ├── EndToEndCompilerTest.java
│       └── TestRunner.java  # Automated test suite runner
├── vscode-extension/        # Visual Studio Code extension
│   ├── assets/              # Extension icon & file-tree icons
│   ├── snippets/            # Language snippets
│   ├── syntaxes/            # TextMate grammar
│   ├── extension.js         # Extension commands & runtime
│   └── package.json         # Extension manifest
├── build.bat                # Windows CMD build script
├── build.ps1                # PowerShell build script
├── update-extension.bat     # One-click extension packager & installer
├── update-extension.ps1     # PowerShell extension updater
├── CONTRIBUTING.md          # Contribution guidelines
├── LICENSE                  # MIT License
└── README.md
```

---

## 🧪 Running Tests

```powershell
.\build.ps1
```

```
==================================================
Running Java Pipe Test Suite
==================================================
[1/4] Running Lexer Tests...
  ✓ Lexer tests passed.
[2/4] Running Parser Tests...
  ✓ Parser tests passed.
[3/4] Running Desugarer Tests...
  ✓ Desugarer tests passed.
[4/4] Running End-to-End Compiler Tests...
  ✓ End-to-end compiler tests passed.
==================================================
Results: 4 suites passed, 0 suites failed
==================================================
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
