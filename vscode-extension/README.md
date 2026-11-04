# Java Pipe for Visual Studio Code

<p align="center">
  <img src="assets/icon.png" width="128" height="128" alt="Java Pipe Logo" />
</p>

Language support, syntax highlighting, snippets, and execution tooling for **Java Pipe (`|>`)** files (`.javap`, `.jpipe`).

---

## Features

- **Syntax Highlighting**: Dedicated syntax highlighting for the pipe forward operator `|>`, method references `::`, lambdas `->`, and placeholders `_`.
- **One-Click Execution**: Run `.javap` files directly with the Play button in the editor title bar.
- **Side-by-Side Transpilation**: Preview desugared standard Java code beside your source with one click.
- **Code Snippets**: Productive snippets for stream pipelines (`jp-stream`), lambdas (`jp-lambda`), and class scaffolding (`jp-main`).
- **Bracket Matching & Indentation**: Smart indentation rules tuned for chained `|>` expressions.

---

## Example Syntax

```java
public class PipelineExample {
    public static void main(String[] args) {
        var result = "  hello modern java  "
            |> String::trim
            |> String::toUpperCase
            |> str -> str + " 🚀";

        System.out.println(result);
    }
}
```

---

## Available Commands

| Command | Title | Action |
| :--- | :--- | :--- |
| `javapipe.runFile` | **Java Pipe: Run Current File** | Executes the file using `javapipec --run` |
| `javapipe.transpile` | **Java Pipe: View Transpiled Java** | Transpiles and opens generated Java in a split editor |
| `javapipe.compile` | **Java Pipe: Compile to Bytecode** | Compiles `.javap` to `.class` files in `out/` |

---

## Installation & Local Development

To test or load this extension into your local VS Code:
1. Open this workspace in VS Code.
2. Press `F5` (or run **Debug: Start Debugging**) to launch an **Extension Development Host** window.
3. Open any `.javap` or `.jpipe` file to experience syntax highlighting and execution tools.
