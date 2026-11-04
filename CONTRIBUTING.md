# Contributing to Java Pipe

Thank you for your interest in contributing to **Java Pipe**! We welcome contributions, bug reports, feature proposals, and pull requests.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 17+** (JDK 17 or JDK 21 recommended)
- **Node.js 18+** (Optional, only required if packaging the VS Code extension via `vsce`)

### Cloning & Building Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/javapipe/javapipe.git
   cd javapipe
   ```

2. Build the compiler and run the test suite:
   - On Windows (PowerShell):
     ```powershell
     .\build.ps1
     ```
   - On Windows (CMD):
     ```batch
     build.bat
     ```
   - On Linux / macOS:
     ```bash
     mkdir -p out bin
     find src/main/java -name "*.java" > sources.txt
     javac -d out @sources.txt
     find src/test/java -name "*.java" > test_sources.txt
     javac -cp out -d out @test_sources.txt
     jar cfe bin/javapipec.jar org.javapipe.cli.JavaPipeCLI -C out .
     java -cp out org.javapipe.TestRunner
     ```

---

## Testing Your Changes

Make sure all automated tests pass before submitting a Pull Request:
```powershell
.\build.ps1
```

You can run the example `.javap` files to test edge cases:
```powershell
bin\javapipec.bat --run examples\01_BasicPipe.javap
bin\javapipec.bat --run examples\05_FullPipeline.javap
```

---

## VS Code Extension Development

1. Navigate to the `vscode-extension` directory:
   ```bash
   cd vscode-extension
   ```
2. Open in VS Code and press `F5` to start the **Extension Development Host**.
3. To package and update your local VS Code extension:
   ```batch
   ..\update-extension.bat
   ```

---

## Pull Request Guidelines

- Ensure your code follows standard Java naming and coding conventions.
- Add unit tests in `src/test/java/org/javapipe/` for any new parser or desugaring features.
- Update documentation or examples if introducing new syntax capabilities.
- Keep commits descriptive and atomic.

