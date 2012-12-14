@echo off

REM **************************************************
REM ** Libraries used by Kettle:                    **
REM **************************************************

set CLASSPATH=.

REM ******************
REM   KETTLE Library
REM ******************
set CLASSPATH=%CLASSPATH%;lib\kettle-core-TRUNK-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-db-TRUNK-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-engine-TRUNK-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;lib\kettle-ui-swt-TRUNK-SNAPSHOT.jar

REM *****************
REM   SWT Libraries
REM *****************

set CLASSPATH=%CLASSPATH%;libswt\runtime.jar
set CLASSPATH=%CLASSPATH%;libswt\jface.jar
set CLASSPATH=%CLASSPATH%;libswt\win32\swt.jar
set CLASSPATH=%CLASSPATH%;libswt\common.jar
set CLASSPATH=%CLASSPATH%;libswt\commands.jar

set CLASSPATH=%CLASSPATH%;lib/kettle-vfs-20091118.jar
set CLASSPATH=%CLASSPATH%;lib/commons-logging-1.1.jar
set CLASSPATH=%CLASSPATH%;lib/log4j-1.2.14.jar
set CLASSPATH=%CLASSPATH%;lib/spring-core-2.5.6.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 128m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx256m -cp %CLASSPATH% -Djava.library.path=libswt\win32\

REM ***************
REM ** Run...    **
REM ***************

java %OPT% org.pentaho.di.ui.i18n.editor.Translator2 %1 %2 %3 %4 %5 %6 %7 %8 %9
