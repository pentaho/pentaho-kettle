@echo off
setlocal

cd /D %~dp0

REM **************************************************
REM ** Kettle home                                  **
REM **************************************************

if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

cd %KETTLE_DIR%

call "%~dp0set-pentaho-env.bat"

REM ***************
REM ** Run...    **
REM ***************

"%_PENTAHO_JAVA%" -jar launcher\launcher.jar -main org.pentaho.di.core.encryption.Encr %*