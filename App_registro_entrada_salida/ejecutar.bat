@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (echo Error al compilar & pause & exit /b 1)
del fuentes.txt
java -Dfile.encoding=UTF-8 -cp out sircap.Main
