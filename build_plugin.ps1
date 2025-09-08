$here = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $here
Write-Host "=== Building ServerPassword plugin ==="
& .\gradlew.bat build
if ($LASTEXITCODE -ne 0) {
  Write-Host "`n*** BUILD FAILED ***" -ForegroundColor Red
  Pause
  exit 1
}
Write-Host "`n*** BUILD SUCCESS ***" -ForegroundColor Green
Write-Host "JAR: build\libs\serverpassword-1.0.0.jar"
Pause
