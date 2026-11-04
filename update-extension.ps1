Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Java Pipe VS Code Extension - Package and Update" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$extDir = Join-Path $scriptDir "vscode-extension"

if (-not (Test-Path (Join-Path $extDir "package.json"))) {
    Write-Error "Cannot find vscode-extension folder at $extDir"
    exit 1
}

Set-Location $extDir

# Clean old .vsix
Get-ChildItem -Filter *.vsix | Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "[1/2] Packaging extension (.vsix)..." -ForegroundColor Yellow
$packaged = $false

try {
    npx -y @vscode/vsce package --allow-missing-repository
    if ($LASTEXITCODE -eq 0) { $packaged = $true }
} catch {
    Write-Warning "vsce packaging failed via npx: $_"
}

$vsix = Get-ChildItem -Filter *.vsix | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($vsix) {
    Write-Host "[OK] Packaged: $($vsix.Name)" -ForegroundColor Green
    Write-Host "`n[2/2] Installing $($vsix.Name) into VS Code..." -ForegroundColor Yellow

    $codeCmd = Get-Command code -ErrorAction SilentlyContinue
    if (-not $codeCmd) {
        $candidate = "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin\code.cmd"
        if (Test-Path $candidate) { $codeCmd = $candidate }
    }

    if ($codeCmd) {
        & $codeCmd --install-extension $vsix.FullName --force
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n===================================================" -ForegroundColor Green
            Write-Host " [SUCCESS] Java Pipe extension successfully installed!" -ForegroundColor Green
            Write-Host " To apply updates in VS Code:" -ForegroundColor White
            Write-Host "   Ctrl+Shift+P -> 'Developer: Reload Window'" -ForegroundColor Yellow
            Write-Host "===================================================" -ForegroundColor Green
            exit 0
        }
    }
}

# Fallback: Direct copy to ~/.vscode/extensions
$targetDir = "$env:USERPROFILE\.vscode\extensions\javapipe-0.1.0"
Write-Host "[INFO] Copying extension files directly to $targetDir..." -ForegroundColor Yellow
if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir | Out-Null }
Copy-Item "$extDir\*" $targetDir -Recurse -Force

Write-Host "`n===================================================" -ForegroundColor Green
Write-Host " [SUCCESS] Java Pipe extension files installed to extensions folder!" -ForegroundColor Green
Write-Host " To apply updates in VS Code:" -ForegroundColor White
Write-Host "   Ctrl+Shift+P -> 'Developer: Reload Window'" -ForegroundColor Yellow
Write-Host "===================================================" -ForegroundColor Green

