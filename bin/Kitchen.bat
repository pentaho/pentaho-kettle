@echo off
setlocal

set CWD=%CD%
cd /D %~dp0

REM **************************************************
REM ** Kettle home                                  **
REM **************************************************
 
if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

cd %KETTLE_DIR%

call "%~dp0set-pentaho-env.bat"

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 512m to higher values in case you run out of memory   **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" set PENTAHO_DI_JAVA_OPTIONS=-Xmx512m

set OPT="%PENTAHO_DI_JAVA_OPTIONS%" "-Djava.library.path=%KETTLE_DIR%\libswt\win32" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%"

REM ***************
REM ** Run...    **
REM ***************

cd /D %CWD%
"%_PENTAHO_JAVA%" %OPT% -jar "%KETTLE_DIR%\launcher\launcher.jar" -main org.pentaho.di.kitchen.Kitchen %*
