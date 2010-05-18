@echo off
setLocal

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

set WorkingDIR=%CD%
cd  %~dp0
cd  ..
set BatchParentDIR=%CD%
cd  %WorkingDIR%

if "%KETTLE_HOME%"=="" set  KETTLE_HOME=%BatchParentDIR%
if %KETTLE_HOME:~-1%==\ set KETTLE_HOME=%KETTLE_HOME:~0,-1%

:: **************************************************
:: ** Libraries used by Kettle:                    **
:: **************************************************

set CP=.;%KETTLE_HOME%

:: ******************
::   KETTLE Library
:: ******************

set CP=%CP%;%KETTLE_HOME%\lib\kettle-core.jar;%KETTLE_HOME%\lib\kettle-db.jar;%KETTLE_HOME%\lib\kettle-engine.jar

:: **********************
::   External Libraries
:: **********************

:: libext
call :AddJarDir "%KETTLE_HOME%\libext"

:: Subdirs of libext
for %%d in (JDBC webservices commons web pentaho spring mondrian salesforce) do call :AddJarDir "%KETTLE_HOME%\libext\%%d"

goto extlibe

:AddJarDir
for %%f in ("%~dpnx1\*.jar") do call :AddCP "%%f"
goto :EOF

:AddCP
set CP=%CP%;%~dpnx1
goto :eof

:extlibe


:: *****************
::   SWT Libraries
:: *****************

set CP=%CP%;%KETTLE_HOME%libswt\runtime.jar
set CP=%CP%;%KETTLE_HOME%libswt\jface.jar
set CP=%CP%;%KETTLE_HOME%libswt\win32\swt.jar

:: ******************************************************************
:: ** Set java runtime options                                     **
:: ** Change 512m to higher values in case you run out of memory.  **
:: ******************************************************************


set OPT=-Xmx512m -cp "%CP%" "-Djava.library.path=%KETTLE_HOME%libswt\win32" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%"

set CP=

:: ***************
:: ** Run...    **
:: ***************

@echo on
echo %KETTLE_HOME%
echo %CP%
echo %OPT%
java %OPT% org.pentaho.di.kitchen.Kitchen %*
