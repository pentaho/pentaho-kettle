@echo off

REM **************************************************
REM ** Make sure we use the correct J2SE version!   **
REM ** Uncomment the PATH line in case of trouble   **
REM **************************************************

REM set PATH=C:\j2sdk1.4.2_01\bin;.;%PATH%

REM **************************************************
REM ** Libraries used by Kettle:                    **
REM **************************************************

set BASEDIR=..

set CLASSPATH=.

REM ******************
REM   KETTLE Library
REM ******************
set CLASSPATH=%CLASSPATH%;%BASEDIR%\lib\kettle3.jar

REM **********************
REM   External Libraries
REM **********************

set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\CacheDB.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\SQLBaseJDBC.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\activation.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\db2jcc.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\db2jcc_license_c.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\edtftpj-1.4.5.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\jaybird-full-2.1.0.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\hsqldb.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\ifxjdbc.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\javadbf.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\js.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\jt400.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\jtds-1.1.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\jxl.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\mail.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\mysql-connector-java-3.1.7-bin.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\ojdbc14.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\orai18n.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\pg74.215.jdbc3.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%\libext\iijdbc.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 128m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx256m -cp %CLASSPATH%

REM ***************
REM ** Run...    **
REM ***************

echo Compiling TransBuilder class
javac -classpath %CLASSPATH% TransBuilder.java

echo Running TransBuilder (see TransBuilder.log)
java %OPT% TransBuilder %1 %2 %3 %4 %5 %6 %7 %8 %9

echo Finished execution

type TransBuilder.log

