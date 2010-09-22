@echo off
setlocal

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

call "%~dp0set-pentaho-env.bat"

:: **************************************************
:: ** Set up usage of JAVA_EXT_LIBS                **
:: **************************************************

if defined JAVA_EXT_DIRS goto :externalExtDirs
:noExternalExtDirs
set JAVA_EXT_DIRS=.
goto endExtDirs

:externalExtDirs
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%~dp0

:endExtDirs

if DEFINED _PENTAHO_JAVA_HOME goto withPentahoJavaHome
goto endPentahoJavaHome
:withPentahoJavaHome
REM Every directory contains the null device. So check
REM for directory existence:
if exist %_PENTAHO_JAVA_HOME%\jre\lib\ext\nul set JAVA_EXT_DIRS=%_PENTAHO_JAVA_HOME%\jre\lib\ext;%JAVA_EXT_DIRS%
if exist %_PENTAHO_JAVA_HOME%\lib\ext\nul set JAVA_EXT_DIRS=%_PENTAHO_JAVA_HOME%\lib\ext;%JAVA_EXT_DIRS%
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%
goto endPentahoJavaHome

:endPentahoJavaHome

REM ******************
REM   KETTLE Library
REM *****************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\lib

REM **********************
REM   External Libraries
REM **********************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\JDBC
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\webservices
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\spring
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\commons
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\web
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\pentaho
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\mondrian
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\salesforce

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ******************************************************************

set OPT="-Djava.ext.dirs=%JAVA_EXT_DIRS%"

if not "%PENTAHO_INSTALLED_LICENSE_PATH%" == "" goto setLicenseVar
goto skipToStartup

:setLicenseVar
set OPT=%OPT% -Dpentaho.installed.licenses.file="%PENTAHO_INSTALLED_LICENSE_PATH%"

:skipToStartup
REM ***************
REM ** Run...    **
REM ***************

"%_PENTAHO_JAVA%" %OPT% org.pentaho.di.core.encryption.Encr %*

