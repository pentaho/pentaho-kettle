@echo off

REM **************************************************
REM ** Set console window properties                **
REM **************************************************
REM TITLE Spoon console
REM COLOR F0

REM **************************************************
REM ** Make sure we use the correct J2SE version!   **
REM ** Uncomment the PATH line in case of trouble   **
REM **************************************************

REM set PATH=C:\j2sdk1.5.0_09\bin;.;%PATH%

REM **************************************************
REM ** Libraries used by Kettle:                    **
REM **************************************************

set CLASSPATH=.

REM ******************
REM   KETTLE Library
REM ******************
set CLASSPATH=%CLASSPATH%;lib\*.jar

REM **********************
REM   External Libraries
REM **********************

REM Loop the libext directory and add the classpath.
REM The following command would only add the last jar: FOR %%F IN (libext\*.jar) DO call set CLASSPATH=%CLASSPATH%;%%F
REM So the circumvention with a subroutine solves this ;-)

set CLASSPATH=%CLASSPATH%;libext\*.jar
set CLASSPATH=%CLASSPATH%;libext\JDBC\*.jar
set CLASSPATH=%CLASSPATH%;libext\webservices\*.jar
set CLASSPATH=%CLASSPATH%;libext\commons\*.jar
set CLASSPATH=%CLASSPATH%;libext\web\*.jar
set CLASSPATH=%CLASSPATH%;libext\pentaho\*.jar
set CLASSPATH=%CLASSPATH%;libext\spring\*.jar

REM **********************
REM   Collect arguments
REM **********************

set _cmdline=
:TopArg
if %1!==! goto EndArg
set _cmdline=%_cmdline% %1
shift
goto TopArg
:EndArg

REM *****************
REM   SWT Libraries
REM *****************

set CLASSPATH=%CLASSPATH%;libswt\*.jar
set CLASSPATH=%CLASSPATH%;libswt\win32\swt.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 256m to higher values in case you run out of memory.  **
REM ******************************************************************

REM set OPT=-Xmx256m -cp %CLASSPATH% -Djava.library.path=libswt\win32\ -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%" -DKETTLE_PLUGIN_PACKAGES="%KETTLE_PLUGIN_PACKAGES%"

set OPT=-Xmx256m -cp %CLASSPATH%

REM ***************
REM ** Run...    **
REM ***************

start javaw %OPT% org.pentaho.di.ui.spoon.Spoon %_cmdline%

