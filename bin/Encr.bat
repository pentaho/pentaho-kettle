@echo off
setlocal

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

if "%KETTLE_HOME%"=="" set KETTLE_HOME=%~dp0
if %KETTLE_HOME:~-1%==\ set KETTLE_HOME=%KETTLE_HOME:~0,-1%

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

if DEFINED JAVA_HOME goto withJavaHome
:noJavaHome
goto endJavaHome

:withJavaHome
REM Every directory contains the null device. So check
REM for directory existence:
if exist %JAVA_HOME%\jre\lib\ext\nul set JAVA_EXT_DIRS=%JAVA_HOME%\jre\lib\ext;%JAVA_EXT_DIRS%
if exist %JAVA_HOME%\lib\ext\nul set JAVA_EXT_DIRS=%JAVA_HOME%\lib\ext;%JAVA_EXT_DIRS%
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;lib
goto endJavaHome

:endJavaHome

REM ******************
REM   KETTLE Library
REM *****************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\lib

REM **********************
REM   External Libraries
REM **********************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\JDBC
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\webservices
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\spring
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\commons
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\web
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\pentaho
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\mondrian
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_HOME%\libext\salesforce

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ******************************************************************

set OPT="-Djava.ext.dirs=%JAVA_EXT_DIRS%"

REM ***************
REM ** Run...    **
REM ***************

java %OPT% org.pentaho.di.core.encryption.Encr %*

