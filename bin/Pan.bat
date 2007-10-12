@echo off

REM **************************************************
REM ** Make sure we use the correct J2SE version!   **
REM ** Uncomment the PATH line in case of trouble   **
REM **************************************************

REM set PATH=C:\j2sdk1.4.2_01\bin;.;%PATH%

REM **************************************************
REM ** Libraries used by Kettle:                    **
REM **************************************************

set CLASSPATH=.

REM ******************
REM   KETTLE Library
REM ******************

set CLASSPATH=%CLASSPATH%;lib\kettle-engine-3.0.jar

REM **********************
REM   External Libraries
REM **********************

REM Loop the libext directory and add the classpath.
REM The following command would only add the last jar: FOR %%F IN (libext\*.jar) DO call set CLASSPATH=%CLASSPATH%;%%F
REM So the circumvention with a subroutine solves this ;-)

FOR %%F IN (libext\*.jar) DO call :addcp %%F
FOR %%F IN (libext\JDBC\*.jar) DO call :addcp %%F
FOR %%F IN (libext\webservices\*.jar) DO call :addcp %%F
FOR %%F IN (libext\commons\*.jar) DO call :addcp %%F
FOR %%F IN (libext\web\*.jar) DO call :addcp %%F
FOR %%F IN (libext\pentaho\*.jar) DO call :addcp %%F
FOR %%F IN (libext\spring\*.jar) DO call :addcp %%F

goto extlibe

:addcp
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:extlibe

REM *****************
REM   SWT Libraries
REM *****************

set CLASSPATH=%CLASSPATH%;libswt\runtime.jar

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


REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 512m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx512M -cp %CLASSPATH% -Djava.library.path=libswt\win32\ -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%" -DKETTLE_PLUGIN_PACKAGES="%KETTLE_PLUGIN_PACKAGES%"

REM ***************
REM ** Run...    **
REM ***************

java %OPT% org.pentaho.di.pan.Pan %_cmdline%
