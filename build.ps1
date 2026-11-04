Write-Host "Building Java Pipe compiler..." -ForegroundColor Cyan
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin | Out-Null }

$files = (Get-ChildItem -Path src/main/java -Recurse -Filter *.java).FullName
javac -d out $files
if ($LASTEXITCODE -ne 0) {
    Write-Error "Main build failed"
    exit 1
}

$testFiles = (Get-ChildItem -Path src/test/java -Recurse -Filter *.java).FullName
javac -cp out -d out $testFiles
if ($LASTEXITCODE -ne 0) {
    Write-Error "Test build failed"
    exit 1
}

jar cfe bin/javapipec.jar org.javapipe.cli.JavaPipeCLI -C out .
Write-Host "Packaging complete: bin/javapipec.jar" -ForegroundColor Green

Write-Host "Running tests..." -ForegroundColor Cyan
java -cp out org.javapipe.TestRunner

