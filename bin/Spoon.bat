@echo off

REM **************************************************
REM Check for Windows 2000, since the command line length is limited
REM **************************************************
ver | find "Windows 2000" >nul
if errorlevel 1 goto nowin2k
echo Attention: You are using Windows 2000. Please see
echo http://wiki.pentaho.org/display/EAI/Windows+2000
echo when Spoon does not start. After fixing the problem
echo you can delete the pause line in your Spoon.bat file.
pause
:nowin2k

REM **************************************************
REM ** Set console window properties                **
REM **************************************************
REM TITLE Spoon console
REM COLOR F0

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

set CLASSPATH=%CLASSPATH%;lib\kettle-core.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-db.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-engine.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-ui-swt.jar

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
FOR %%F IN (libext\jfree\*.jar) DO call :addcp %%F
FOR %%F IN (libext\mondrian\*.jar) DO call :addcp %%F

goto extlibe

:addcp
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:extlibe

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

set CLASSPATH=%CLASSPATH%;libswt\runtime.jar
set CLASSPATH=%CLASSPATH%;libswt\jface.jar
set CLASSPATH=%CLASSPATH%;libswt\common.jar
set CLASSPATH=%CLASSPATH%;libswt\commands.jar
set CLASSPATH=%CLASSPATH%;libswt\win32\swt.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 256m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx256m -cp %CLASSPATH% -Djava.library.path=libswt\win32\ -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%" -DKETTLE_PLUGIN_PACKAGES="%KETTLE_PLUGIN_PACKAGES%"

REM ***************
REM ** Run...    **
REM ***************

start javaw %OPT% org.pentaho.di.ui.spoon.Spoon %_cmdline%
