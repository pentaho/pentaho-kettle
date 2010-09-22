@echo off
setlocal

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

cd %KETTLE_DIR%

call "%~dp0set-pentaho-env.bat"

REM **************************************************
REM ** Set up usage of JAVA_EXT_LIBS                **
REM **************************************************

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
FOR /F %%a IN ('""%_PENTAHO_JAVA%" -version" 2^>^&1^|find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
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

echo %LIBSPATH%

REM *******************************************
REM   Collect arguments from the command line
REM *******************************************

set _cmdline=
:TopArg
if %1!==! goto EndArg
set _cmdline=%_cmdline% %1
shift
goto TopArg
:EndArg

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 512m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx512M "-Djava.ext.dirs=%JAVA_EXT_DIRS%" "-Dorg.mortbay.util.URI.charset=UTF-8" "-Djava.library.path=%LIBSPATH%" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%"

if not "%PENTAHO_INSTALLED_LICENSE_PATH%" == "" goto setLicenseVar
goto skipToStartup

:setLicenseVar
set OPT=%OPT% -Dpentaho.installed.licenses.file="%PENTAHO_INSTALLED_LICENSE_PATH%"

:skipToStartup

REM ***********************************************************************
REM ** Optionally set up the options for JAAS (uncomment to make active) **
REM ***********************************************************************

REM set OPT=%OPT% -Djava.security.auth.login.config=%JAAS_LOGIN_MODULE_CONFIG%
REM set	OPT=%OPT% -Dloginmodulename=%JAAS_LOGIN_MODULE_NAME%

REM ***************
REM ** Run...    **
REM ***************

"%_PENTAHO_JAVA%" %OPT% org.pentaho.di.www.Carte %_cmdline%

