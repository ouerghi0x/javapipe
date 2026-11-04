const vscode = require('vscode');
const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {
    const outputChannel = vscode.window.createOutputChannel('Java Pipe');

    // 1. Command: Run current file
    const runCmd = vscode.commands.registerCommand('javapipe.runFile', () => {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active Java Pipe (.javap) file open.');
            return;
        }

        const filePath = editor.document.fileName;
        if (!filePath.endsWith('.javap') && !filePath.endsWith('.jpipe')) {
            vscode.window.showWarningMessage('Active file is not a .javap or .jpipe file.');
            return;
        }

        const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri?.fsPath || path.dirname(filePath);
        const jarPath = path.join(workspaceFolder, 'bin', 'javapipec.jar');

        const terminal = vscode.window.activeTerminal || vscode.window.createTerminal('Java Pipe');
        terminal.show();

        if (fs.existsSync(jarPath)) {
            terminal.sendText(`java -jar "${jarPath}" --run "${filePath}"`);
        } else {
            terminal.sendText(`javapipec --run "${filePath}"`);
        }
    });

    // 2. Command: Transpile to Standard Java
    const transpileCmd = vscode.commands.registerCommand('javapipe.transpile', () => {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active Java Pipe (.javap) file open.');
            return;
        }

        const filePath = editor.document.fileName;
        const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri?.fsPath || path.dirname(filePath);
        const jarPath = path.join(workspaceFolder, 'bin', 'javapipec.jar');
        const cmd = fs.existsSync(jarPath) ? `java -jar "${jarPath}" --transpile "${filePath}"` : `javapipec --transpile "${filePath}"`;

        outputChannel.clear();
        outputChannel.show(true);
        outputChannel.appendLine(`Transpiling ${path.basename(filePath)}...`);

        exec(cmd, { cwd: workspaceFolder }, async (error, stdout, stderr) => {
            if (error) {
                vscode.window.showErrorMessage(`Transpilation error: ${stderr || error.message}`);
                outputChannel.appendLine(stderr || error.message);
                return;
            }

            // Open transpiled code in a new side-by-side Java document
            const doc = await vscode.workspace.openTextDocument({
                content: stdout,
                language: 'java'
            });
            await vscode.window.showTextDocument(doc, { viewColumn: vscode.ViewColumn.Beside, preview: true });
        });
    });

    // 3. Command: Compile to Bytecode
    const compileCmd = vscode.commands.registerCommand('javapipe.compile', () => {
        const editor = vscode.window.activeTextEditor;
        if (!editor) return;

        const filePath = editor.document.fileName;
        const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri?.fsPath || path.dirname(filePath);
        const jarPath = path.join(workspaceFolder, 'bin', 'javapipec.jar');
        const cmd = fs.existsSync(jarPath) ? `java -jar "${jarPath}" -d out "${filePath}"` : `javapipec -d out "${filePath}"`;

        outputChannel.clear();
        outputChannel.show(true);
        outputChannel.appendLine(`Compiling ${path.basename(filePath)}...`);

        exec(cmd, { cwd: workspaceFolder }, (error, stdout, stderr) => {
            if (error) {
                vscode.window.showErrorMessage(`Compilation failed. See Output for details.`);
                outputChannel.appendLine(stderr || error.message);
            } else {
                vscode.window.showInformationMessage(`Successfully compiled ${path.basename(filePath)}`);
                outputChannel.appendLine(`Done! Bytecode written to out/`);
            }
        });
    });

    context.subscriptions.push(runCmd, transpileCmd, compileCmd);
}

function deactivate() {}

module.exports = {
    activate,
    deactivate
};
