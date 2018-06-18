@echo off
if ""%1"" == ""-p"" goto doSetPort
if ""%1"" == """" goto doStart

echo Usage:  %0 [OPTIONS]
echo   -p [port]          Server port (default: 8899)
goto end

:doSetPort
shift
set PORT=%1

:doStart
set CFG_DIR=%~dp0%..
set CLASSPATH=%CFG_DIR%
set CLASSPATH=%~dp0..\lib\*;%CLASSPATH%
set CONSOLE_MAIN=io.elasticjob.lite.console.ConsoleBootstrap
echo on
if ""%PORT%"" == """" set PORT=8899
java  -cp "%CLASSPATH%" %CONSOLE_MAIN% %PORT%

:end
