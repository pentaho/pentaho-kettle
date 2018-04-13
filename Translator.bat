@echo on

REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2006-2018 by Hitachi Vantara : http://www.pentaho.com
REM
REM *****************************************************************************
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with
REM the License. You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM *****************************************************************************

REM **************************************************
REM ** Libraries used by Kettle:                    **
REM **************************************************

set RUNDIR=%CD%\dist
set XMLFILE=%CD%\%1%
set SRCDIR=%CD%\%2%

echo RUNDIR=%RUNDIR%
echo XMLFILE=%XMLFILE%
echo SRCDIR=%SRCDIR%

cd dist

set IS64BITJAVA=0

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
REM Below is a logic to find the directory where java can found. We will
REM temporarily change the directory to that folder where we can run java there

FOR /F %%a IN ('java -version 2^>^&1^|find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
GOTO CHECK32VS64BITJAVA
:CHECK32VS64BITJAVA

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
set SWTJAR=..\libswt\win64

:CONTINUE


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
REM ** Change 512m to higher values in case you run out of memory   **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

set PENTAHO_DI_JAVA_OPTIONS="-Xmx512m" "-XX:MaxPermSize=256m"
set OPT="-Djava.library.path=%LIBSPATH%" "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%" "-DKETTLE_JNDI_ROOT=%KETTLE_JNDI_ROOT%"

REM ***************
REM ** Run...    **
REM ***************

REM Eventually call java instead of javaw and do not run in a separate window
set TRANSLATOR_START_OPTION=start "Translator"

@echo on
%TRANSLATOR_START_OPTION% java %OPT% -jar launcher\launcher-1.0.0.jar -lib ..\%LIBSPATH% -main org.pentaho.di.ui.i18n.editor.Translator2 %XMLFILE% %SRCDIR%
@echo off

cd %RUNDIR%\..
