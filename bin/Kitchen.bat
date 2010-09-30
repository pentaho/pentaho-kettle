@echo off
setlocal

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************
 
if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

cd %KETTLE_DIR%

call "%~dp0set-pentaho-env.bat"

:: **************************************************
:: ** Set up usage of JAVA_EXT_LIBS                **
:: **************************************************

if defined JAVA_EXT_DIRS goto :externalExtDirs
:noExternalExtDirs
set JAVA_EXT_DIRS=.
goto endExtDirs

:externalExtDirs
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;

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

:: **************************************************
:: ** Libraries used by Kettle:                    **
:: **************************************************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\lib

:: **********************
::   External Libraries
:: **********************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\JDBC
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\webservices
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\spring
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\commons
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\web
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\pentaho
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\mondrian
set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%KETTLE_DIR%\libext\salesforce

:: *****************
::   SWT Libraries
:: *****************

set JAVA_EXT_DIRS=%JAVA_EXT_DIRS%;%~dp0libswt

:: ******************************************************************
:: ** Set java runtime options                                     **
REM ** Change 512m to higher values in case you run out of memory   **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" set PENTAHO_DI_JAVA_OPTIONS=-Xmx512m

set OPT="%PENTAHO_DI_JAVA_OPTIONS%" "-Djava.ext.dirs=%JAVA_EXT_DIRS%" "-Djava.library.path=%~dp0libswt\win32" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%"

:: ***************
:: ** Run...    **
:: ***************

"%_PENTAHO_JAVA%" %OPT% org.pentaho.di.kitchen.Kitchen %*
