\
@echo off
setlocal EnableDelayedExpansion

set DIST_VER=8.10.2
set DIST_NAME=gradle-%DIST_VER%-bin.zip
set DIST_URL=https://services.gradle.org/distributions/%DIST_NAME%
set INSTALL_DIR=.gradle-local\gradle-%DIST_VER%

if not exist "%INSTALL_DIR%\bin\gradle.bat" (
  echo -> Bootstrapping Gradle %DIST_VER%...
  if not exist ".gradle-local" mkdir .gradle-local
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$url='%DIST_URL%'; $out='%INSTALL_DIR%\%DIST_NAME%';" ^
    "New-Item -ItemType Directory -Force -Path '%INSTALL_DIR%' | Out-Null;" ^
    "Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $out; " ^
    "Expand-Archive -Force -Path $out -DestinationPath '.gradle-local'; " ^
    "Remove-Item -Force $out"
  if ERRORLEVEL 1 (
    echo ERROR: Failed to download/extract Gradle. Check your internet connection or run with admin PowerShell.
    exit /b 1
  )
)

call "%INSTALL_DIR%\bin\gradle.bat" %*
