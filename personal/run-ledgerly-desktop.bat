@echo off
cd /d "%~dp0"
mvn -q -DskipTests package
if errorlevel 1 (
  echo Build failed. Check Maven and JDK 21.
  pause
  exit /b 1
)
java -jar target\personal-expense-tracker-0.1.0.jar --desktop
if errorlevel 1 pause
