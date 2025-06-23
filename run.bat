@echo off
cls
set filename=%1

if "%filename%"=="" (
    echo Uso: run.bat archivo.ll
    goto end
)

rem Compilar el .ll a .exe
echo Compilando...
clang %filename% -o output.exe

if exist output.exe (
    echo ===============================
    echo Ejecutando %filename%
    echo ==== Resultado ====
    output.exe
    echo ==== Fin ====
    
) else (
    echo Error: No se pudo compilar el archivo.
)

pause
:end
