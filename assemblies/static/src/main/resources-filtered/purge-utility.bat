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
call "%~dp0set-pentaho-env.bat"

SET "JAVA_ADD_OPENS="
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.net=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
SET "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"

"%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% -Xmx2048m -classpath "%~dp0plugins\pdi-pur-plugin\*;%~dp0lib\*;%~dp0classes" com.pentaho.di.purge.RepositoryCleanupUtil %*
