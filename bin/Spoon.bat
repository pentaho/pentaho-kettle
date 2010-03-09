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

set LIBSPATH=-lib ..\libswt\win32

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

set OPT=-Xmx256m -Djava.library.path=..\libswt\win32\ -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%" -DKETTLE_PLUGIN_PACKAGES="%KETTLE_PLUGIN_PACKAGES%" -DKETTLE_LOG_SIZE_LIMIT="%KETTLE_LOG_SIZE_LIMIT%"

REM ***************
REM ** Run...    **
REM ***************

start javaw %OPT% -jar launcher\launcher.jar %LIBSPATH% %_cmdline%
