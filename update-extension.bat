@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Java Pipe VS Code Extension - Package and Update
echo ===================================================
echo.

set "SCRIPT_DIR=%~dp0"
set "EXT_DIR=%SCRIPT_DIR%vscode-extension"

if not exist "%EXT_DIR%\package.json" (
    echo [ERROR] Cannot find vscode-extension folder at %EXT_DIR%
    pause
    exit /b 1
)

cd /d "%EXT_DIR%"

echo [1/2] Packaging extension (.vsix)...
del /q *.vsix 2>nul

:: Try packaging with vsce, fallback to npx @vscode/vsce
set "PACKAGED=0"
where vsce >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Running vsce package...
    call vsce package --allow-missing-repository
    if !ERRORLEVEL! EQU 0 set "PACKAGED=1"
)

if "!PACKAGED!"=="0" (
    echo [INFO] Running npx @vscode/vsce package...
    call npx -y @vscode/vsce package --allow-missing-repository
    if !ERRORLEVEL! EQU 0 set "PACKAGED=1"
)

:: Find the generated .vsix file
set "VSIX_FILE="
for /f "delims=" %%F in ('dir /b /o:-d *.vsix 2^>nul') do (
    set "VSIX_FILE=%%F"
    goto :found_vsix
)

:found_vsix
if "%VSIX_FILE%"=="" (
    echo [WARNING] Packaging .vsix was skipped or failed.
    echo [INFO] Falling back to direct extension folder installation...
    goto :direct_install
)

echo [OK] Packaged: !VSIX_FILE!
echo.
echo [2/2] Installing !VSIX_FILE! into VS Code...

:: Locate code command - prioritize full path to code.cmd
set "CODE_CMD="
if exist "%LOCALAPPDATA%\Programs\Microsoft VS Code\bin\code.cmd" (
    set "CODE_CMD=%LOCALAPPDATA%\Programs\Microsoft VS Code\bin\code.cmd"
) else if exist "%ProgramFiles%\Microsoft VS Code\bin\code.cmd" (
    set "CODE_CMD=%ProgramFiles%\Microsoft VS Code\bin\code.cmd"
) else (
    where code.cmd >nul 2>nul
    if !ERRORLEVEL! EQU 0 (
        for /f "delims=" %%C in ('where code.cmd') do (
            if not defined CODE_CMD set "CODE_CMD=%%C"
        )
    ) else (
        where code >nul 2>nul
        if !ERRORLEVEL! EQU 0 set "CODE_CMD=code"
    )
)

if defined CODE_CMD (
    echo [INFO] Using: "!CODE_CMD!"
    call "!CODE_CMD!" --install-extension "!VSIX_FILE!" --force
    if !ERRORLEVEL! EQU 0 (
        goto :success
    ) else (
        echo [WARNING] "code --install-extension" exited with error. Trying direct sync...
    )
) else (
    echo [INFO] "code" CLI not found. Using direct extension folder installation...
)

:direct_install
set "TARGET_DIR=%USERPROFILE%\.vscode\extensions\javapipe-0.1.0"
echo [INFO] Copying extension files directly to !TARGET_DIR!...
if not exist "!TARGET_DIR!" mkdir "!TARGET_DIR!"
xcopy /E /I /Y /Q "%EXT_DIR%\*" "!TARGET_DIR!\" 2>nul
if %ERRORLEVEL% LEQ 1 (
    echo [OK] Files copied successfully to VS Code extensions directory.
    goto :success
) else (
    echo [ERROR] Failed to install extension.
    pause
    exit /b 1
)

:success
echo.
echo ===================================================
echo  [SUCCESS] Java Pipe extension successfully installed!
echo.
echo  To apply updates in your running VS Code:
echo    1. Open VS Code
echo    2. Press Ctrl+Shift+P
echo    3. Select: "Developer: Reload Window"
echo ===================================================
echo.
pause
exit /b 0
