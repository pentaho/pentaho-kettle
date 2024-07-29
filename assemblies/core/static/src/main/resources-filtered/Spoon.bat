@echo off

REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2005 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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

setlocal

cd /D %~dp0

REM **************************************************
REM ** Set console window properties                **
REM **************************************************
REM TITLE Spoon console
REM COLOR F0

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

if "%KETTLE_DIR%"=="" set KETTLE_DIR=%~dp0
if %KETTLE_DIR:~-1%==\ set KETTLE_DIR=%KETTLE_DIR:~0,-1%

cd %KETTLE_DIR%

REM Special console/debug options when called from SpoonConsole.bat or SpoonDebug.bat
if "%SPOON_CONSOLE%"=="1" set PENTAHO_JAVA=java.exe
if not "%SPOON_CONSOLE%"=="1" set PENTAHO_JAVA=javaw.exe
set IS64BITJAVA=0

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
REM Below is a logic to find the directory where java can found. We will
REM temporarily change the directory to that folder where we can run java there
pushd "%_PENTAHO_JAVA_HOME%"
if exist java.exe goto USEJAVAFROMPENTAHOJAVAHOME
cd bin
if exist java.exe goto USEJAVAFROMPENTAHOJAVAHOME
popd
pushd "%_PENTAHO_JAVA_HOME%\jre\bin"
if exist java.exe goto USEJAVAFROMPATH
goto USEJAVAFROMPATH
:USEJAVAFROMPENTAHOJAVAHOME
FOR /F %%a IN ('.\java.exe -version 2^>^&1^|%windir%\system32\find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
FOR /F %%a IN ('.\java.exe -version 2^>^&1^|%windir%\system32\find /C "version ""1.8."') DO (SET /a ISJAVA8=%%a)
GOTO CHECK32VS64BITJAVA
:USEJAVAFROMPATH
FOR /F %%a IN ('java -version 2^>^&1^|%windir%\system32\find /C "64-Bit"') DO (SET /a IS64BITJAVA=%%a)
FOR /F %%a IN ('java -version 2^>^&1^|%windir%\system32\find /C "version ""1.8."') DO (SET /a ISJAVA8=%%a)
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
popd

REM **************************************************
REM ** Setup Karaf endorsed libraries directory     **
REM **************************************************
set JAVA_ENDORSED_DIRS=
set JAVA_LOCALE_COMPAT=
set JAVA_ADD_OPENS=

IF NOT %ISJAVA8% == 1 GOTO :SKIPENDORSEDJARS

if not "%_PENTAHO_JAVA_HOME%" == "" set JAVA_ENDORSED_DIRS=%_PENTAHO_JAVA_HOME%\jre\lib\endorsed;%_PENTAHO_JAVA_HOME%\lib\endorsed;
set JAVA_ENDORSED_DIRS="-Djava.endorsed.dirs=%JAVA_ENDORSED_DIRS%%KETTLE_DIR%\system\karaf\lib\endorsed"
GOTO :COLLECTARGUMENTS

:SKIPENDORSEDJARS
REM required for Java 11 date/time formatting backwards compatibility
set JAVA_LOCALE_COMPAT=-Djava.locale.providers=COMPAT,SPI
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.io=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.net=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.security=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.util=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.reflect.misc=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.management/javax.management=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.management/javax.management.openmbean=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.math=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.nio=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"

:COLLECTARGUMENTS
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
REM ** Change 2048m to higher values in case you run out of memory  **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" set PENTAHO_DI_JAVA_OPTIONS="-Xms1024m" "-Xmx2048m"

set OPT=%OPT% %PENTAHO_DI_JAVA_OPTIONS% "-Djava.library.path=%LIBSPATH%;%HADOOP_HOME%/bin" %JAVA_ENDORSED_DIRS% %JAVA_LOCALE_COMPAT% "-DKETTLE_HOME=%KETTLE_HOME%" "-DKETTLE_REPOSITORY=%KETTLE_REPOSITORY%" "-DKETTLE_USER=%KETTLE_USER%" "-DKETTLE_PASSWORD=%KETTLE_PASSWORD%" "-DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES%" "-DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT%" "-DKETTLE_JNDI_ROOT=%KETTLE_JNDI_ROOT%"

REM Force SWT to use Edge instead of Internet Explorer (not supported by Pentaho anymore)
set OPT=%OPT% "-Dorg.eclipse.swt.browser.DefaultType=edge"

REM ***************
REM ** Run...    **
REM ***************

if %STARTTITLE%!==! SET STARTTITLE="Spoon"
REM Eventually call java instead of javaw and do not run in a separate window
if not "%SPOON_CONSOLE%"=="1" set SPOON_START_OPTION=start %STARTTITLE%

@echo on
%SPOON_START_OPTION% "%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% %OPT% -jar launcher\launcher.jar -lib ..\%LIBSPATH% %_cmdline%
@echo off
if "%SPOON_PAUSE%"=="1" pause
