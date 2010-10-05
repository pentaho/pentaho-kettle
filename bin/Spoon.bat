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
REM   Spoon Plugins and Platform Specific SWT       **
REM **************************************************

REM The following line is predicated on the 64-bit Sun
REM java output from -version which
REM looks like this (at the time of this writing):
REM
REM java version "1.6.0_17"
REM Java(TM) SE Runtime Environment (build 1.6.0_17-b04)
REM Java HotSpot(TM) 64-Bit Server VM (build 14.3-b01, mixed mode)
REM
FOR /F %%a IN ('java -version 2^>^&1^|find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
IF %IS64BITJAVA% == 1 GOTO :USE64
:USE32
REM ===========================================
REM Using 32bit Java, so include 32bit SWT Jar
REM ===========================================
set LIBSPATH=libswt\win32
GOTO :CONTINUE
:USE64
REM ===========================================
REM Using 64bit java, so include 64bit SWT Jar
REM ===========================================
set LIBSPATH=libswt\win64
:CONTINUE

REM FOR /D %%F IN (plugins\spoon\*) DO call :addpp %%F

goto extlibe

:addpp
set LIBSPATH=%LIBSPATH%;..\%1\lib
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


REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 256m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx256m -Xms256m -Djava.library.path=%LIBSPATH% -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%" -DKETTLE_PLUGIN_PACKAGES="%KETTLE_PLUGIN_PACKAGES%" -DKETTLE_LOG_SIZE_LIMIT="%KETTLE_LOG_SIZE_LIMIT%"

REM ***************
REM ** Run...    **
REM ***************

start javaw %OPT% -jar launcher\launcher-1.0.0.jar -lib %LIBSPATH% %_cmdline%
