@echo off
setlocal
echo Building Java Pipe compiler...
if not exist out mkdir out
if not exist bin mkdir bin

dir /s /b src\main\java\*.java > sources.txt
javac -d out @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo Main compilation failed!
    exit /b %ERRORLEVEL%
)

dir /s /b src\test\java\*.java > test_sources.txt
javac -cp out -d out @test_sources.txt
del test_sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo Test compilation failed!
    exit /b %ERRORLEVEL%
)

jar cfe bin\javapipec.jar org.javapipe.cli.JavaPipeCLI -C out .
echo Packaging complete: bin\javapipec.jar

echo Running test suite...
java -cp out org.javapipe.TestRunner

