@echo off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2029-07-20
REM ******************************************************************************

setlocal

cd /D "%~dp0"

REM **************************************************
REM ** Set console window properties                **
REM **************************************************
REM TITLE Spoon console
REM COLOR F0

:: **************************************************
:: ** Kettle home                                  **
:: **************************************************

if "%KETTLE_DIR%"=="" SET "KETTLE_DIR=%~dp0"
if "%KETTLE_DIR:~-1%"=="\" SET "KETTLE_DIR=%KETTLE_DIR:~0,-1%"

cd "%KETTLE_DIR%"

REM Special console/debug options when called from SpoonConsole.bat or SpoonDebug.bat
if "%SPOON_CONSOLE%"=="1" SET "PENTAHO_JAVA=java.exe"
if not "%SPOON_CONSOLE%"=="1" SET "PENTAHO_JAVA=javaw.exe"
SET "IS64BITJAVA=0"

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
SET "LIBSPATH=libswt\win32"
GOTO :CONTINUE
:USE64
REM ===========================================
REM Using 64bit java, so include 64bit SWT Jar
REM ===========================================

SET "LIBSPATH=libswt\win64;native-lib\win64;..\native-lib\win64"
SET "SWTJAR=..\libswt\win64"
:CONTINUE
popd

REM **************************************************
REM ** Setup Karaf endorsed libraries directory     **
REM **************************************************
SET "JAVA_LOCALE_COMPAT="
SET "JAVA_ADD_OPENS="

REM required for Java 11 date/time formatting backwards compatibility
SET "JAVA_LOCALE_COMPAT=-Djava.locale.providers=COMPAT,SPI"
REM Sets options that only get read by Java 11 to remove illegal reflective access warnings
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.io=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.net=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.security=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.util=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.reflect.misc=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.management/javax.management=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.management/javax.management.openmbean=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.math=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.nio=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"

:COLLECTARGUMENTS
REM **********************
REM   Collect arguments
REM **********************

SET "_cmdline="
:TopArg
if %1!==! goto EndArg
SET "_cmdline=%_cmdline% %1"
shift
goto TopArg
:EndArg

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 2048m to higher values in case you run out of memory  **
REM ** or set the PENTAHO_DI_JAVA_OPTIONS environment variable      **
REM ******************************************************************

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" SET "PENTAHO_DI_JAVA_OPTIONS=-Xms1024m -Xmx2048m"

SET "OPT=%OPT% %PENTAHO_DI_JAVA_OPTIONS% -Djava.library.path=%LIBSPATH%;%HADOOP_HOME%/bin %JAVA_LOCALE_COMPAT% -DKETTLE_HOME=%KETTLE_HOME% -DKETTLE_REPOSITORY=%KETTLE_REPOSITORY% -DKETTLE_USER=%KETTLE_USER% -DKETTLE_PASSWORD=%KETTLE_PASSWORD% -DKETTLE_PLUGIN_PACKAGES=%KETTLE_PLUGIN_PACKAGES% -DKETTLE_LOG_SIZE_LIMIT=%KETTLE_LOG_SIZE_LIMIT% -DKETTLE_JNDI_ROOT=%KETTLE_JNDI_ROOT%"

REM Add this option to allow orc's compatibility with protobuf-java 3.25.6 libraries
SET "OPT=%OPT% -Dcom.google.protobuf.use_unsafe_pre22_gencode=true"

REM Force SWT to use Edge instead of Internet Explorer (not supported by Pentaho anymore)
SET "OPT=%OPT% -Dorg.eclipse.swt.browser.DefaultType=edge"

REM ***************
REM ** Run...    **
REM ***************

if %STARTTITLE%!==! SET "STARTTITLE=Spoon"
REM Eventually call java instead of javaw and do not run in a separate window
if not "%SPOON_CONSOLE%"=="1" SET "SPOON_START_OPTION=start %STARTTITLE%"

@echo on
%SPOON_START_OPTION% "%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% %OPT% -jar launcher\launcher.jar -lib "..\%LIBSPATH%" %_cmdline%
@echo off
if "%SPOON_PAUSE%"=="1" pause
