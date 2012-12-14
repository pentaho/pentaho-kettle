@echo off

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

REM *****************
REM   SWT Libraries
REM *****************

set CLASSPATH=%CLASSPATH%;libswt\runtime.jar
set CLASSPATH=%CLASSPATH%;libswt\jface.jar
set CLASSPATH=%CLASSPATH%;libswt\win32\swt.jar
set CLASSPATH=%CLASSPATH%;libswt\common.jar
set CLASSPATH=%CLASSPATH%;libswt\commands.jar

set CLASSPATH=%CLASSPATH%;libext/pentaho/kettle-vfs-20091118.jar
set CLASSPATH=%CLASSPATH%;libext/commons/commons-logging-1.1.jar
set CLASSPATH=%CLASSPATH%;libext/log4j-1.2.14.jar
set CLASSPATH=%CLASSPATH%;libext/spring/spring-core-2.5.6.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 128m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx256m -cp %CLASSPATH% -Djava.library.path=libswt\win32\

REM ***************
REM ** Run...    **
REM ***************

java %OPT% org.pentaho.di.ui.i18n.editor.Translator2 %1 %2 %3 %4 %5 %6 %7 %8 %9
