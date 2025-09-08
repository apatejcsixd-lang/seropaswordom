@echo off
setlocal
cd /d "%~dp0"
echo === Building ServerPassword plugin ===
echo (Don't type a backslash after this finishes.)
call gradlew.bat build
if errorlevel 1 (
  echo.
  echo *** BUILD FAILED ***
  pause
  exit /b 1
)
echo.
echo *** BUILD SUCCESS ***
echo JAR: build\libs\serverpassword-1.0.0.jar
pause
