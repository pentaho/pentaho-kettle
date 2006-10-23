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

set CLASSPATH=%CLASSPATH%;lib\kettle.jar

REM **********************
REM   External Libraries
REM **********************

set CLASSPATH=%CLASSPATH%;libext\CacheDB.jar
set CLASSPATH=%CLASSPATH%;libext\SQLBaseJDBC.jar
set CLASSPATH=%CLASSPATH%;libext\activation.jar
set CLASSPATH=%CLASSPATH%;libext\db2jcc.jar
set CLASSPATH=%CLASSPATH%;libext\db2jcc_license_cu.jar
set CLASSPATH=%CLASSPATH%;libext\edtftpj-1.5.3.jar
set CLASSPATH=%CLASSPATH%;libext\firebirdsql-full.jar
set CLASSPATH=%CLASSPATH%;libext\firebirdsql.jar
set CLASSPATH=%CLASSPATH%;libext\gis-shape.jar
set CLASSPATH=%CLASSPATH%;libext\hsqldb.jar
set CLASSPATH=%CLASSPATH%;libext\ifxjdbc.jar
set CLASSPATH=%CLASSPATH%;libext\javadbf.jar
set CLASSPATH=%CLASSPATH%;libext\jconn2.jar
set CLASSPATH=%CLASSPATH%;libext\js.jar
set CLASSPATH=%CLASSPATH%;libext\jt400.jar
set CLASSPATH=%CLASSPATH%;libext\jtds-1.2.jar
set CLASSPATH=%CLASSPATH%;libext\jxl.jar
set CLASSPATH=%CLASSPATH%;libext\ktable.jar
set CLASSPATH=%CLASSPATH%;libext\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;libext\mail.jar
set CLASSPATH=%CLASSPATH%;libext\mysql-connector-java-3.1.13-bin.jar
set CLASSPATH=%CLASSPATH%;libext\ojdbc14.jar
set CLASSPATH=%CLASSPATH%;libext\orai18n.jar
set CLASSPATH=%CLASSPATH%;libext\postgresql-8.1-407.jdbc3.jar
set CLASSPATH=%CLASSPATH%;libext\edbc.jar
set CLASSPATH=%CLASSPATH%;libext\jsch-0.1.24.jar
set CLASSPATH=%CLASSPATH%;libext\interclient.jar
set CLASSPATH=%CLASSPATH%;libext\sapdbc.jar
set CLASSPATH=%CLASSPATH%;libext\xdbjdbc.jar
set CLASSPATH=%CLASSPATH%;libext\rdbthin.jar
set CLASSPATH=%CLASSPATH%;libext\jackcess-1.1.5.jar
set CLASSPATH=%CLASSPATH%;libext\commons-collections-3.1.jar
set CLASSPATH=%CLASSPATH%;libext\commons-logging.jar
set CLASSPATH=%CLASSPATH%;libext\commons-lang-2.2.jar
set CLASSPATH=%CLASSPATH%;libext\commons-dbcp-1.2.1.jar
set CLASSPATH=%CLASSPATH%;libext\commons-pool-1.3.jar
set CLASSPATH=%CLASSPATH%;libext\nzjdbc.jar

REM *****************
REM   SWT Libraries
REM *****************

set CLASSPATH=%CLASSPATH%;libswt\runtime.jar
set CLASSPATH=%CLASSPATH%;libswt\jface.jar
set CLASSPATH=%CLASSPATH%;libswt\win32\swt.jar

REM ******************************************************************
REM ** Set java runtime options                                     **
REM ** Change 128m to higher values in case you run out of memory.  **
REM ******************************************************************

set OPT=-Xmx512M -cp %CLASSPATH% -Djava.library.path=libswt\win32\ -DKETTLE_HOME="%KETTLE_HOME%" -DKETTLE_REPOSITORY="%KETTLE_REPOSITORY%" -DKETTLE_USER="%KETTLE_USER%" -DKETTLE_PASSWORD="%KETTLE_PASSWORD%"

REM ***************
REM ** Run...    **
REM ***************

java %OPT% be.ibridge.kettle.pan.Pan %1 %2 %3 %4 %5 %6 %7 %8 %9

