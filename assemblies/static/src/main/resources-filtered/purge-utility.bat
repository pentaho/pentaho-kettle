@echo off

REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2006 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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
call "%~dp0set-pentaho-env.bat"

set JAVA_ADD_OPENS=
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.lang=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/java.net=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED"
set "JAVA_ADD_OPENS=%JAVA_ADD_OPENS% --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED"

"%_PENTAHO_JAVA%" %JAVA_ADD_OPENS% -Xmx2048m -classpath "%~dp0plugins\pdi-pur-plugin\*;%~dp0lib\*;%~dp0classes" com.pentaho.di.purge.RepositoryCleanupUtil %*
