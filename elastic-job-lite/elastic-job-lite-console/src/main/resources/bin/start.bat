@echo off
set DDCFGDIR=%~dp0%..\conf
set CLASSPATH=%DDCFGDIR%
SET CLASSPATH=%~dp0..\lib\*;%CLASSPATH%
set CONSOLE_MAIN=com.dangdang.ddframe.job.lite.console.ConsoleBootstrap
echo on
java  -cp "%CLASSPATH%" %CONSOLE_MAIN% 