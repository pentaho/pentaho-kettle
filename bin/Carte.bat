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

REM **************************************************
REM   Platform Specific SWT       **
REM **************************************************

REM The following line is predicated on the 64-bit Sun
REM java output from -version which
REM looks like this (at the time of this writing):
REM
REM java version "1.6.0_17"
REM Java(TM) SE Runtime Environment (build 1.6.0_17-b04)
REM Java HotSpot(TM) 64-Bit Server VM (build 14.3-b01, mixed mode)
REM
REM
REM Below is a logic to find the directory where java can found. We will
REM temporarily change the directory to that folder where we can run java there
pushd "%_PENTAHO_JAVA_HOME%"
if exist java.exe goto GOTJAVA
cd bin
if exist java.exe goto GOTJAVA
popd
pushd "%_PENTAHO_JAVA_HOME%\jre\bin"
if exist java.exe goto GOTJAVA
goto USE32
:GOTJAVA
FOR /F %%a IN ('.\java.exe -version 2^>^&1^|%windir%\system32\find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
IF %IS64BITJAVA% == 1 GOTO :USE64
:USE32
REM ===========================================
REM Using 32bit Java, so include 32bit SWT Jar
REM ===========================================
set LIBSPATH=%KETTLE_DIR%\libswt\win32
GOTO :CONTINUE
:USE64
REM ===========================================
REM Using 64bit java, so include 64bit SWT Jar
REM ===========================================
set LIBSPATH=%KETTLE_DIR%\libswt\win64
:CONTINUE
popd

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 512m to higher values in case you run out of memory   **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" set PENTAHO_DI_JAVA_OPTIONS=-Xmx512m

set OPT="%PENTAHO_DI_JAVA_OPTIONS%" "-Dorg.mortbay.util.URI.charset=UTF-8" "-Djava.library.path=%LIBSPATH%" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%"

REM ***********************************************************************
REM ** Optionally set up the options for JAAS (uncomment to make active) **
REM ***********************************************************************

REM set OPT=%OPT% -Djava.security.auth.login.config=%JAAS_LOGIN_MODULE_CONFIG%
REM set	OPT=%OPT% -Dloginmodulename=%JAAS_LOGIN_MODULE_NAME%

REM ***************
REM ** Run...    **
REM ***************

cd /D %CWD%
"%_PENTAHO_JAVA%" %OPT% -jar %KETTLE_DIR%\launcher\launcher.jar -main org.pentaho.di.www.Carte %*

